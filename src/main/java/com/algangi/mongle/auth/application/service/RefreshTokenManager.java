package com.algangi.mongle.auth.application.service;

import com.algangi.mongle.auth.domain.model.RefreshToken;

public interface RefreshTokenManager {

    RefreshToken generate(Long memberId);

    void validateToken(String token);

    Long getUserId(String token);


}
