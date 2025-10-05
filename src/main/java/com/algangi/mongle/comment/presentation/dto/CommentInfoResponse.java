package com.algangi.mongle.comment.presentation.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentInfoResponse(
        String commentId,
        String content,
        AuthorInfoResponse author,
        long likeCount,
        long dislikeCount,
        String myReaction,
        LocalDateTime createdAt,
        boolean isAuthor,
        boolean isDeleted,
        boolean hasReplies
) {

}