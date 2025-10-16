package com.algangi.mongle.comment.presentation.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record CommentInfoResponse(
        String commentId,
        String content,
        AuthorInfoResponse author,
        long likeCount,
        long dislikeCount,
        String myReaction,
        Instant createdAt,
        boolean isAuthor,
        boolean isDeleted,
        boolean hasReplies
) {

}