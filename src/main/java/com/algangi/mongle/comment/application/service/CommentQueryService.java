package com.algangi.mongle.comment.application.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.model.CommentSort;
import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;
import com.algangi.mongle.comment.presentation.cursor.CursorConvertible;
import com.algangi.mongle.comment.presentation.dto.CommentInfoResponse;
import com.algangi.mongle.comment.presentation.cursor.CursorInfoResponse;
import com.algangi.mongle.comment.presentation.dto.ReplyInfoResponse;
import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.comment.presentation.mapper.CommentResponseMapper;
import com.algangi.mongle.post.service.PostFinder;
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

        // 3. 댓글 조회(hasNext 확인을 위해 +1만큼 조회)
        List<Comment> comments = commentQueryRepository.findCommentsByPost(condition, adjustedSize + 1);

        // 4. 다음 페이지 존재 여부 판단 & 실제 반환 리스트 자르기
        boolean hasNext = comments.size() > adjustedSize;
        List<Comment> content =
                hasNext ? comments.subList(0, adjustedSize)
                        : comments;

        // 5. 각 댓글의 대댓글 존재 여부 Map<댓글ID, Boolean> 형태로 조회
        Map<Long, Boolean> hasRepliesMap = getHasRepliesMap(content);

        // 6. Dto 변환
        List<CommentInfoResponse> responses = content.stream()
                .map(comment -> commentResponseMapper.toCommentInfoResponse(
                        comment,
                        currentMemberId,
                        hasRepliesMap.getOrDefault(comment.getId(), false)))
                .toList();

        // 7. 커서 생성
        String nextCursor = createNextCursor(responses, hasNext, condition.sort());

        return CursorInfoResponse.of(responses, nextCursor, hasNext);
    }

    public CursorInfoResponse<ReplyInfoResponse> getRepliesByParent(
            ReplySearchCondition condition, Long currentMemberId, int pageSize) {
        // 1. 부모 댓글 존재 확인
        commentFinder.getCommentOrThrow(condition.parentId());

        // 2. 페이지 사이즈 조정
        int adjustedSize = clampPageSize(pageSize);

        // 3. 대댓글 조회(hasNext 확인을 위해 +1만큼 조회)
        List<Comment> replies = commentQueryRepository.findRepliesByParent(condition, adjustedSize + 1);

        // 4. 다음 페이지 존재 여부 판단 & 실제 반환 리스트 자르기
        boolean hasNext = replies.size() > adjustedSize;
        List<Comment> content =
                hasNext ? replies.subList(0, adjustedSize)
                        : replies;

        // 5. Dto 변환
        List<ReplyInfoResponse> responses = content.stream()
                .map(reply -> commentResponseMapper.toReplyInfoResponse(
                        reply,
                        currentMemberId))
                .toList();

        // 6. 커서 생성
        String nextCursor = createNextCursor(responses, hasNext, condition.sort());

        return CursorInfoResponse.of(responses, nextCursor, hasNext);
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