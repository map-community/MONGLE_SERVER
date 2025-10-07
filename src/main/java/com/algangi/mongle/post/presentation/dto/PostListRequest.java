package com.algangi.mongle.post.presentation.dto;

import java.util.Optional;

public record PostListRequest(
    String placeId,
    String cloudId,
    String cursor,
    Integer size,
    PostSort sortBy
) {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public PostSort sortBy() {
        return Optional.ofNullable(sortBy).orElse(PostSort.ranking_score);
    }

    public Integer size() {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}