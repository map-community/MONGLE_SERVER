package com.algangi.mongle.auth.presentation.dto;

public record TokenInfo(
    String tokenType,
    String accessToken,
    String refreshToken,
    Long accessTokenExpirationMillis,
    Long refreshTokenExpirationMillis
) {

    public static TokenInfo of(
        String tokenType,
        String accessToken,
        String refreshToken,
        Long accessTokenExpirationMillis,
        Long refreshTokenExpirationMillis

    ) {
        return new TokenInfo(tokenType, accessToken, refreshToken, accessTokenExpirationMillis,
            refreshTokenExpirationMillis);
    }
}
