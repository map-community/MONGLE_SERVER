package com.algangi.mongle.auth.application.service;

import com.algangi.mongle.auth.domain.model.RefreshToken;

public interface RefreshTokenManager {

    RefreshToken generate(String memberId);

    void validateToken(String token);

    String getUserId(String token);


}
