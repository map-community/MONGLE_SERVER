package com.algangi.mongle.comment.application.service;

import com.algangi.mongle.block.application.service.BlockQueryService;
import com.algangi.mongle.comment.application.vo.CommentStats;
import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.model.CommentSort;
import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.PaginationResult;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;
import com.algangi.mongle.comment.domain.model.CursorConvertible;
import com.algangi.mongle.comment.presentation.dto.CommentInfoResponse;
import com.algangi.mongle.comment.presentation.cursor.CursorInfoResponse;
import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.comment.presentation.mapper.CommentResponseMapper;
import com.algangi.mongle.global.util.DateTimeUtil;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.reaction.application.service.ReactionQueryService;
import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.stats.application.service.StatsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final BlockQueryService blockQueryService;
    private final StatsQueryService statsQueryService;
    private final ReactionQueryService reactionQueryService;

    private static final int MAX_PAGE_SIZE = 50;

    public CursorInfoResponse<CommentInfoResponse> getCommentsByPost(
            CommentSearchCondition condition, String currentMemberId, int pageSize) {
        // 1. 게시글 존재 확인
        postFinder.getPostOrThrow(condition.postId());

        // 2. 페이지 사이즈 조정
        int adjustedSize = clampPageSize(pageSize);

        // 3. 차단 목록 조회
        List<String> blockedMemberIds = blockQueryService.getBlockedUserIds(currentMemberId);

        // 4. 댓글 조회
        PaginationResult<Comment> pageResult = commentQueryRepository.findCommentsByPost(condition, adjustedSize,blockedMemberIds);
        List<Comment> comments = pageResult.content();

        if (comments.isEmpty()) {
            return CursorInfoResponse.empty();
        }

        // 5. redis에서 리액션 조회
        List<String> commentIds = comments.stream().map(Comment::getId).toList();
        Map<String, CommentStats> statsMap = statsQueryService.getCommentStatsMap(commentIds);

        // 6. 현재 사용자 리액션 조회
        Map<String, ReactionType> myReactionsMap = reactionQueryService.getMyReactions(
                TargetType.COMMENT,
                commentIds,
                currentMemberId
        );

        // 7. 각 댓글의 대댓글 존재 여부 Map<댓글ID, Boolean> 형태로 조회
        Map<String, Boolean> hasRepliesMap = getHasRepliesMap(pageResult.content());

        // 8. 커서 생성
        String nextCursor = createNextCursor(pageResult.content(), pageResult.hasNext(), condition.sort());

        // 9. Dto 변환
        List<CommentInfoResponse> responses = comments.stream()
                .map(comment -> {
                    CommentStats stats = statsMap.getOrDefault(comment.getId(), CommentStats.empty());
                    boolean hasReplies = hasRepliesMap.getOrDefault(comment.getId(), false);
                    ReactionType myReaction = myReactionsMap.get(comment.getId());
                    return commentResponseMapper.toCommentInfoResponse(
                            comment,
                            currentMemberId,
                            hasReplies,
                            stats.likes(),
                            stats.dislikes(),
                            (myReaction != null) ? myReaction.name() : null
                    );
                })
                .toList();

        return CursorInfoResponse.of(responses, nextCursor, pageResult.hasNext());
    }

    public CursorInfoResponse<CommentInfoResponse> getRepliesByParent(
            ReplySearchCondition condition, String currentMemberId, int pageSize) {
        // 1. 부모 댓글 존재 확인
        commentFinder.getCommentOrThrow(condition.parentId());

        // 2. 페이지 사이즈 조정
        int adjustedSize = clampPageSize(pageSize);

        // 3. 차단 목록 조회
        List<String> blockedMemberIds = blockQueryService.getBlockedUserIds(currentMemberId);

        // 4. 대댓글 조회(hasNext 확인을 위해 +1만큼 조회)
        PaginationResult<Comment> pageResult = commentQueryRepository.findRepliesByParent(condition, adjustedSize, blockedMemberIds);
        List<Comment> replies = pageResult.content();

        if (replies.isEmpty()) {
            return CursorInfoResponse.empty();
        }

        // 5. redis에서 리액션 조회
        List<String> replyIds = replies.stream().map(Comment::getId).toList();
        Map<String, CommentStats> statsMap = statsQueryService.getCommentStatsMap(replyIds);

        // 6. 현재 사용자 리액션 조회
        Map<String, ReactionType> myReactionsMap = reactionQueryService.getMyReactions(
                TargetType.COMMENT,
                replyIds,
                currentMemberId
        );

        // 7. 커서 생성
        String nextCursor = createNextCursor(pageResult.content(), pageResult.hasNext(), condition.sort());

        // 8. Dto 변환
        List<CommentInfoResponse> responses = replies.stream()
                .map(reply -> {
                    CommentStats stats = statsMap.getOrDefault(reply.getId(), CommentStats.empty());
                    ReactionType myReaction = myReactionsMap.get(reply.getId());
                    return commentResponseMapper.toCommentInfoResponse(
                            reply,
                            currentMemberId,
                            false,
                            stats.likes(),
                            stats.dislikes(),
                            (myReaction != null) ? myReaction.name() : null
                    );
                })
                .toList();

        return CursorInfoResponse.of(responses, nextCursor, pageResult.hasNext());
    }

    private Map<String, Boolean> getHasRepliesMap(List<Comment> comments) {
        if (comments.isEmpty()) return Map.of();

        List<String> parentIds = comments.stream()
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
                .format(DateTimeUtil.CURSOR_DATE_FORMATTER);

        return switch (sort) {
            case LIKES -> String.format("%d_%s_%s", lastItem.getLikeCount(), formattedDate, lastItem.getId());
            case LATEST -> String.format("%s_%s", formattedDate, lastItem.getId());
        };
    }
}