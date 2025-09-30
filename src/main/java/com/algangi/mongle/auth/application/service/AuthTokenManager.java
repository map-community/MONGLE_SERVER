package com.algangi.mongle.auth.application.service;

import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.domain.model.AccessToken;
import com.algangi.mongle.auth.domain.model.RefreshToken;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.domain.MemberRole;
import com.algangi.mongle.member.service.MemberFinder;

@Service
public class AuthTokenManager {

    private static final String BEARER_TYPE = "bearer";
    private final AccessTokenManager accessTokenManager;
    private final RefreshTokenManager refreshTokenManager;
    private final MemberFinder memberFinder;

    public AuthTokenManager(AccessTokenManager accessTokenManager,
        RefreshTokenManager refreshTokenManager, MemberFinder memberFinder) {
        this.accessTokenManager = accessTokenManager;
        this.refreshTokenManager = refreshTokenManager;
        this.memberFinder = memberFinder;
    }

    public TokenInfo generateTokens(Long memberId, MemberRole role) {
        AccessToken accessToken = accessTokenManager.generate(memberId, role);
        RefreshToken refreshToken = refreshTokenManager.generate(memberId);

        return TokenInfo.of(BEARER_TYPE, accessToken, refreshToken);
    }

    public TokenInfo reissueTokens(String refreshToken) {
        refreshTokenManager.validateToken(refreshToken);

        Long userId = refreshTokenManager.getUserId(refreshToken);
        Member member = memberFinder.getMemberOrThrow(userId);

        return generateTokens(member.getMemberId(), member.getMemberRole());
    }


}
