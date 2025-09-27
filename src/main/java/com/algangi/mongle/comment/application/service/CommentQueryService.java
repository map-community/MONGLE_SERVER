package com.algangi.mongle.comment.application.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.model.CommentSort;
import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.PaginationResult;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;
import com.algangi.mongle.comment.domain.model.CursorConvertible;
import com.algangi.mongle.comment.presentation.dto.CommentInfoResponse;
import com.algangi.mongle.comment.presentation.cursor.CursorInfoResponse;
import com.algangi.mongle.comment.presentation.dto.ReplyInfoResponse;
import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.comment.presentation.mapper.CommentResponseMapper;
import com.algangi.mongle.post.application.helper.PostFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentQueryRepository commentQueryRepository;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;
    private final CommentResponseMapper commentResponseMapper;

    private static final int MAX_PAGE_SIZE = 50;

    public CursorInfoResponse<CommentInfoResponse> getCommentsByPost(
            CommentSearchCondition condition, Long currentMemberId, int pageSize) {
        // 1. 게시글 존재 확인
        postFinder.getPostOrThrow(condition.postId());

        // 2. 페이지 사이즈 조정
        int adjustedSize = clampPageSize(pageSize);

        // 3. 댓글 조회
        PaginationResult<Comment> pageResult = commentQueryRepository.findCommentsByPost(condition, adjustedSize);

        // 4. 각 댓글의 대댓글 존재 여부 Map<댓글ID, Boolean> 형태로 조회
        Map<Long, Boolean> hasRepliesMap = getHasRepliesMap(pageResult.content());

        // 5. 커서 생성
        String nextCursor = createNextCursor(pageResult.content(), pageResult.hasNext(), condition.sort());

        // 6. Dto 변환
        List<CommentInfoResponse> responses = pageResult.content().stream()
                .map(comment -> commentResponseMapper.toCommentInfoResponse(
                        comment,
                        currentMemberId,
                        hasRepliesMap.getOrDefault(comment.getId(), false)))
                .toList();

        return CursorInfoResponse.of(responses, nextCursor, pageResult.hasNext());
    }

    public CursorInfoResponse<ReplyInfoResponse> getRepliesByParent(
            ReplySearchCondition condition, Long currentMemberId, int pageSize) {
        // 1. 부모 댓글 존재 확인
        commentFinder.getCommentOrThrow(condition.parentId());

        // 2. 페이지 사이즈 조정
        int adjustedSize = clampPageSize(pageSize);

        // 3. 대댓글 조회(hasNext 확인을 위해 +1만큼 조회)
        PaginationResult<Comment> pageResult = commentQueryRepository.findRepliesByParent(condition, adjustedSize);

        // 4. 커서 생성
        String nextCursor = createNextCursor(pageResult.content(), pageResult.hasNext(), condition.sort());

        // 5. Dto 변환
        List<ReplyInfoResponse> responses = pageResult.content().stream()
                .map(reply -> commentResponseMapper.toReplyInfoResponse(
                        reply,
                        currentMemberId))
                .toList();

        return CursorInfoResponse.of(responses, nextCursor, pageResult.hasNext());
    }

    private Map<Long, Boolean> getHasRepliesMap(List<Comment> comments) {
        if (comments.isEmpty()) return Map.of();

        List<Long> parentIds = comments.stream()
                .map(Comment::getId)
                .toList();
        return commentQueryRepository.findHasRepliesByParentIds(parentIds);
    }

    private int clampPageSize(int size) {
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }

    private <T extends CursorConvertible> String createNextCursor(List<T> results, boolean hasNext, CommentSort sort) {
        if (!hasNext || results.isEmpty()) {
            return null;
        }

        T lastItem = results.get(results.size() - 1);
        String formattedDate = lastItem.getCreatedAt()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return switch (sort) {
            case LIKES -> String.format("%d_%s_%d", lastItem.getLikeCount(), formattedDate, lastItem.getId());
            case LATEST -> String.format("%s_%d", formattedDate, lastItem.getId());
        };
    }
}