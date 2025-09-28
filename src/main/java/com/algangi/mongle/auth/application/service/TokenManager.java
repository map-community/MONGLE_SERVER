package com.algangi.mongle.auth.application.service;

import org.springframework.security.core.Authentication;

import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.member.domain.MemberRole;

public interface TokenManager {

    public TokenInfo generateTokens(Long memberId, MemberRole role);

    public Authentication getAuthentication(String accessToken);

    public boolean validateToken(String token);

}
