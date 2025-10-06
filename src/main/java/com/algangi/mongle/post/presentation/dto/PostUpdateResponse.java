package com.algangi.mongle.post.presentation.dto;

public record PostUpdateResponse(
    String id,
    String content
) {

    public static PostUpdateResponse of(String id, String content) {
        return new PostUpdateResponse(id, content);
    }
}
