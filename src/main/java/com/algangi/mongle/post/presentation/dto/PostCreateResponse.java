package com.algangi.mongle.post.presentation.dto;

import com.algangi.mongle.post.domain.model.Post;

import java.time.LocalDateTime;

public record PostCreateResponse(
    String id,
    String content,
    String authorId,
    String s2TokenId,
    Long staticCloudId,
    Long dynamicCloudId,
    LocalDateTime createdAt
) {

    public static PostCreateResponse from(Post post) {
        return new PostCreateResponse(
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

