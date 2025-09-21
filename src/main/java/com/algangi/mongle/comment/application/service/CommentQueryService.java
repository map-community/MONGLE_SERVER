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

        postFinder.getPostOrThrow(condition.postId());

        int adjustedSize = clampPageSize(pageSize);

        List<Comment> comments = commentQueryRepository.findCommentsByPost(condition, adjustedSize + 1);

        boolean hasNext = comments.size() > adjustedSize;
        List<Comment> content = hasNext ? comments.subList(0, adjustedSize) : comments;

        Map<Long, Boolean> hasRepliesMap = getHasRepliesMap(content);

        List<CommentInfoResponse> responses = content.stream()
                .map(comment -> commentResponseMapper.toCommentInfoResponse(comment, currentMemberId,
                        hasRepliesMap.getOrDefault(comment.getId(), false)))
                .toList();

        return CursorInfoResponse.of(responses, createNextCursor(responses, hasNext, condition.sort()));
    }

    public CursorInfoResponse<ReplyInfoResponse> getRepliesByParent(
            ReplySearchCondition condition, Long currentMemberId, int pageSize) {

        commentFinder.getCommentOrThrow(condition.parentId());

        int adjustedSize = clampPageSize(pageSize);

        List<Comment> replies = commentQueryRepository.findRepliesByParent(condition, adjustedSize + 1);

        boolean hasNext = replies.size() > adjustedSize;
        List<Comment> content = hasNext ? replies.subList(0, adjustedSize) : replies;

        List<ReplyInfoResponse> responses = content.stream()
                .map(reply -> commentResponseMapper.toReplyInfoResponse(reply, currentMemberId))
                .toList();

        return CursorInfoResponse.of(responses, createNextCursor(responses, hasNext, condition.sort()));
    }

    private Map<Long, Boolean> getHasRepliesMap(List<Comment> comments) {
        if (comments.isEmpty()) return Map.of();

        List<Long> parentIds = comments.stream().map(Comment::getId).toList();
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
        String formattedDate = lastItem.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return switch (sort) {
            case LIKES -> String.format("%d_%s_%d", lastItem.getLikeCount(), formattedDate, lastItem.getId());
            case LATEST -> String.format("%s_%d", formattedDate, lastItem.getId());
        };
    }
}