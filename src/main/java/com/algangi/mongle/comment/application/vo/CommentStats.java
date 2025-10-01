package com.algangi.mongle.comment.application.vo;

public record CommentStats(
        long likes,
        long dislikes
) {
    public static CommentStats empty() {
        return new CommentStats(0L, 0L);
    }

}