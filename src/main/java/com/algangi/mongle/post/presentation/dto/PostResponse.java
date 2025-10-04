package com.algangi.mongle.post.presentation.dto;

import com.algangi.mongle.post.domain.model.Post;

import java.time.LocalDateTime;

public record PostResponse(
    String id,
    String content,
    String authorId,
    String s2TokenId,
    Long staticCloudId,
    Long dynamicCloudId,
    LocalDateTime createdAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getContent(),
            post.getAuthorId(),
            post.getS2TokenId(),
            post.getStaticCloudId(),
            post.getDynamicCloudId(),
            post.getCreatedDate()
        );
    }
}

