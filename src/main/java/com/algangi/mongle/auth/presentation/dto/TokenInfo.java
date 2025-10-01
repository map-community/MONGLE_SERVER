package com.algangi.mongle.auth.presentation.dto;

import com.algangi.mongle.auth.domain.model.AccessToken;
import com.algangi.mongle.auth.domain.model.RefreshToken;

public record TokenInfo(
    String tokenType,
    String accessToken,
    String refreshToken,
    Long accessTokenExpirationMillis,
    Long refreshTokenExpirationMillis
) {

    public static TokenInfo of(
        String tokenType,
        AccessToken accessToken,
        RefreshToken refreshToken
    ) {
        return new TokenInfo(tokenType, accessToken.accessToken(), refreshToken.getRefreshToken(),
            accessToken.expirationMillis(),
            refreshToken.getExpirationMillis());
    }
}
