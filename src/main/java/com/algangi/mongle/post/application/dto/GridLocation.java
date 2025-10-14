package com.algangi.mongle.post.application.dto;

import com.algangi.mongle.post.domain.model.Location;

public record GridLocation(
    String s2TokenId,
    Location location
) {

    public static GridLocation of(String s2TokenId, Location location) {
        return new GridLocation(s2TokenId, location);
    }
}
