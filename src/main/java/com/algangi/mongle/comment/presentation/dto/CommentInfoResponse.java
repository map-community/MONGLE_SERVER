package com.algangi.mongle.comment.presentation.dto;

import java.time.LocalDateTime;

public record CommentInfoResponse(
        String commentId,
        String content,
        AuthorInfoResponse author,
        long likeCount,
        long dislikeCount,
        LocalDateTime createdAt,
        boolean isAuthor,
        boolean isDeleted,
        boolean hasReplies
) {

}