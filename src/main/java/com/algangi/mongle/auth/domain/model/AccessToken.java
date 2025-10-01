package com.algangi.mongle.auth.domain.model;

public record AccessToken(
    String accessToken,
    Long expirationMillis
) {

}
