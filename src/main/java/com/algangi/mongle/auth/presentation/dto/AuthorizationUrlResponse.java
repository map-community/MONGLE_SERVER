package com.algangi.mongle.auth.presentation.dto;

public record AuthorizationUrlResponse(
    String authorizationUrl
) {

    public static AuthorizationUrlResponse from(String authorizationUrl) {
        return new AuthorizationUrlResponse(authorizationUrl);
    }
}
