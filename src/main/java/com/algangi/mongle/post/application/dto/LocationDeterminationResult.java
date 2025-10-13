package com.algangi.mongle.post.application.dto;

import com.algangi.mongle.post.domain.model.Location;

public record LocationDeterminationResult(
    String s2TokenId,
    Location location
) {

    public static LocationDeterminationResult of(String s2TokenId, Location location) {
        return new LocationDeterminationResult(s2TokenId, location);
    }
}
