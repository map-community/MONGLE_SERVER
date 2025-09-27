package com.algangi.mongle.comment.presentation.dto;

import java.time.LocalDateTime;

public record ReplyInfoResponse(
        String replyId,
        String content,
        AuthorInfoResponse author,
        long likeCount,
        long dislikeCount,
        LocalDateTime createdAt,
        boolean isAuthor,
        boolean isDeleted
) {

}