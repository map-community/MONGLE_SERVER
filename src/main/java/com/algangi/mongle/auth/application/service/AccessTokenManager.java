package com.algangi.mongle.auth.application.service;

import org.springframework.security.core.Authentication;

import com.algangi.mongle.auth.domain.model.AccessToken;
import com.algangi.mongle.member.domain.MemberRole;

public interface AccessTokenManager {

    AccessToken generate(String memberId, MemberRole role);

    Authentication getAuthentication(String accessToken);

    void validateToken(String token);

    String getUserId(String token);
}