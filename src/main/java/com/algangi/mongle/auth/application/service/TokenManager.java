package com.algangi.mongle.auth.application.service;

import org.springframework.security.core.Authentication;

import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.member.domain.MemberRole;

public interface TokenManager {

    TokenInfo generateTokens(Long memberId, MemberRole role);

    Authentication getAuthentication(String accessToken);

    boolean validateToken(String token);

    Long getUserId(String token);

}
