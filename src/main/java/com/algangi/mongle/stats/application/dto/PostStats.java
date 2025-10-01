package com.algangi.mongle.stats.application.dto;

public record PostStats(
    long viewCount,
    long commentCount
) {

    public static PostStats empty() {
        return new PostStats(0L, 0L);
    }
}