package com.algangi.mongle.post.application.dto;

import com.algangi.mongle.post.domain.model.Location;

public record PostCreationCommand(
    Location location,
    String s2TokenId,
    String title,
    String content,
    Long authorId
) {

}
