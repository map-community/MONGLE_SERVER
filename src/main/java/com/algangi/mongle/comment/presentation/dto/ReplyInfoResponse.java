package com.algangi.mongle.comment.presentation.dto;

import com.algangi.mongle.comment.presentation.cursor.CursorConvertible;

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
) implements CursorConvertible {

    @Override
    public Long getId() {  return replyId(); }

    @Override
    public long getLikeCount() { return likeCount(); }

    @Override
    public LocalDateTime getCreatedAt() { return createdAt(); }

}