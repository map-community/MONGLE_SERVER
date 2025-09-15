package com.algangi.mongle.comment.service;

import com.algangi.mongle.comment.domain.Comment;
import com.algangi.mongle.comment.domain.CommentSort;
import com.algangi.mongle.comment.dto.CommentInfoResponse;
import com.algangi.mongle.comment.dto.CursorInfoResponse;
import com.algangi.mongle.comment.repository.CommentQueryRepository;
import com.algangi.mongle.post.service.PostFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentQueryRepository commentQueryRepository;
    private final PostFinder postFinder;

    private static final int MAX_PAGE_SIZE = 50;

    public CursorInfoResponse<CommentInfoResponse> getCommentList(
            Long postId,
            String cursor,
            int size,
            CommentSort sort,
            Long currentMemberId) {

        postFinder.getPostOrThrow(postId);

        int adjustedSize = Math.min(size, MAX_PAGE_SIZE);

        List<Comment> parentComments = commentQueryRepository.findParentCommentsWithCursor(postId, cursor, adjustedSize, sort);

        if (parentComments.isEmpty()) {
            return CursorInfoResponse.empty();
        }

        boolean hasNext = parentComments.size() > adjustedSize;
        List<Comment> actualParentComments = hasNext ? parentComments.subList(0, adjustedSize) : parentComments;

        List<CommentInfoResponse> dtos = mapToCommentResponses(actualParentComments, currentMemberId);

        String nextCursor = createNextCursor(hasNext, actualParentComments, sort);

        return new CursorInfoResponse<>(dtos, nextCursor, hasNext);
    }

    private List<CommentInfoResponse> mapToCommentResponses(List<Comment> parentComments, Long currentMemberId) {
        Map<Long, List<Comment>> repliesMap = groupRepliesByParentId(parentComments);

        return parentComments.stream()
                .map(parent -> {
                    List<Comment> repliesForParent = repliesMap.getOrDefault(parent.getId(), Collections.emptyList());

                    List<CommentInfoResponse> replyDtos = repliesForParent.stream()
                            .map(reply -> CommentInfoResponse.fromChild(reply, currentMemberId))
                            .toList();

                    return CommentInfoResponse.fromParent(parent, currentMemberId, replyDtos);
                })
                .toList();
    }

    private Map<Long, List<Comment>> groupRepliesByParentId(List<Comment> parentComments) {
        List<Long> parentIds = parentComments.stream().map(Comment::getId).toList();
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Comment> replies = commentQueryRepository.findRepliesByParentIds(parentIds);

        return replies.stream()
                .collect(Collectors.groupingBy(
                        reply -> reply.getParentComment().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private String createNextCursor(boolean hasNext, List<Comment> actualParentComments, CommentSort sort) {
        if (!hasNext) {
            return null;
        }

        Comment lastComment = actualParentComments.get(actualParentComments.size() - 1);

        return switch (sort) {
            case LIKES -> lastComment.getLikeCount() + "_" + lastComment.getId();
            case LATEST -> String.valueOf(lastComment.getId());
        };
    }

}