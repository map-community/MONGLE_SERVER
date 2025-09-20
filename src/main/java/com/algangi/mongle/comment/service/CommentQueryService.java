package com.algangi.mongle.comment.service;

import com.algangi.mongle.comment.domain.Comment;
import com.algangi.mongle.comment.domain.CommentSort;
import com.algangi.mongle.comment.dto.CommentInfoResponse;
import com.algangi.mongle.comment.dto.CursorInfoResponse;
import com.algangi.mongle.comment.dto.ReplyInfoResponse;
import com.algangi.mongle.comment.repository.CommentQueryRepository;
import com.algangi.mongle.post.service.PostFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.algangi.mongle.comment.dto.CommentMapper.toCommentInfoResponse;
import static com.algangi.mongle.comment.dto.CommentMapper.toReplyInfoResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentQueryRepository commentRepository;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    private static final int MAX_PAGE_SIZE = 50;

    public CursorInfoResponse<CommentInfoResponse> getCommentsByPost(
            Long postId, String cursor, int size, CommentSort sort, Long currentMemberId) {

        postFinder.getPostOrThrow(postId);
        int adjustedSize = Math.min(size, MAX_PAGE_SIZE);

        List<Comment> comments = commentRepository.findCommentEntitiesByPost(postId, cursor, adjustedSize + 1, sort);

        boolean hasNext = comments.size() > adjustedSize;
        List<Comment> content = hasNext ? comments.subList(0, adjustedSize) : comments;

        Map<Long, Boolean> hasRepliesMap = getHasRepliesMap(content);

        List<CommentInfoResponse> responses = content.stream()
                .map(comment -> toCommentInfoResponse(comment, currentMemberId,
                        hasRepliesMap.getOrDefault(comment.getId(), false)))
                .toList();

        return CursorInfoResponse.of(responses, createNextCursor(responses, hasNext, sort));
    }

    public CursorInfoResponse<ReplyInfoResponse> getRepliesByParent(
            Long parentId, String cursor, int size, CommentSort sort, Long currentMemberId) {

        commentFinder.getCommentOrThrow(parentId);
        int adjustedSize = Math.min(size, MAX_PAGE_SIZE);

        List<Comment> replies = commentRepository.findReplyEntitiesByParent(parentId, cursor, adjustedSize + 1, sort);

        boolean hasNext = replies.size() > adjustedSize;
        List<Comment> content = hasNext ? replies.subList(0, adjustedSize) : replies;

        List<ReplyInfoResponse> responses = content.stream()
                .map(reply -> toReplyInfoResponse(reply, currentMemberId))
                .toList();

        return CursorInfoResponse.of(responses, createNextCursor(responses, hasNext, sort));
    }

    private Map<Long, Boolean> getHasRepliesMap(List<Comment> comments) {
        if (comments.isEmpty()) return Map.of();
        List<Long> parentIds = comments.stream().map(Comment::getId).toList();
        return commentRepository.findHasRepliesByParentIds(parentIds);
    }

    private <T> String createNextCursor(List<T> results, boolean hasNext, CommentSort sort) {
        if (!hasNext || results.isEmpty()) return null;

        Object lastItem = results.get(results.size() - 1);
        return switch (sort) {
            case LIKES -> {
                if (lastItem instanceof CommentInfoResponse r) {
                    yield r.likeCount() + "_" + r.commentId();
                } else if (lastItem instanceof ReplyInfoResponse r) {
                    yield r.likeCount() + "_" + r.replyId();
                } else yield null;
            }
            case LATEST -> {
                if (lastItem instanceof CommentInfoResponse r) {
                    yield String.valueOf(r.commentId());
                } else if (lastItem instanceof ReplyInfoResponse r) {
                    yield String.valueOf(r.replyId());
                } else yield null;
            }
        };
    }
}