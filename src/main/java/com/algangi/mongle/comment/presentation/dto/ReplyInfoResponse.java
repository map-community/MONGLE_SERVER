package com.algangi.mongle.comment.presentation.dto;

import java.time.LocalDateTime;

public record ReplyInfoResponse(
        Long replyId,
        String content,
        String authorNickname,
        String authorProfileImageUrl,
        long likeCount,
        long dislikeCount,
        LocalDateTime createdAt,
        boolean isAuthor,
        boolean isDeleted
) {

}