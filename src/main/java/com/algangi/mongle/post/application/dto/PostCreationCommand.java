package com.algangi.mongle.post.application.dto;

import com.algangi.mongle.post.domain.model.Location;

public record PostCreationCommand(
    String id,
    Location location,
    String s2TokenId,
    String title,
    String content,
    Long authorId
) {

    public static PostCreationCommand of(String id, Location location, String s2TokenId,
        String title, String content, Long authorId) {
        return new PostCreationCommand(id, location, s2TokenId, title, content, authorId);
    }

}
