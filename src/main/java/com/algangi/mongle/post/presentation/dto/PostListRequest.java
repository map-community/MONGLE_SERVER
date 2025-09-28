package com.algangi.mongle.post.presentation.dto;

import java.util.Optional;

public record PostListRequest(
    String placeId,
    String cloudId,
    PostSort sortBy,
    String cursor,
    Integer size
) {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public PostSort sortBy() {
        return Optional.ofNullable(sortBy).orElse(PostSort.createdAt);
    }

    public int size() {
        return Optional.ofNullable(size)
            .map(s -> Math.min(s, MAX_SIZE))
            .orElse(DEFAULT_SIZE);
    }
}
