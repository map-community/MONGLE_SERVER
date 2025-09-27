package com.algangi.mongle.comment.presentation.dto;

import java.time.LocalDateTime;

public record CommentInfoResponse(
        Long commentId,
        String content,
        String authorNickname,
        String authorProfileImageUrl,
        long likeCount,
        long dislikeCount,
        LocalDateTime createdAt,
        boolean isAuthor,
        boolean isDeleted,
        boolean hasReplies
) {

}