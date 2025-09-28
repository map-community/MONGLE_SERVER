package com.algangi.mongle.auth.application.service;

import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.domain.model.RefreshToken;
import com.algangi.mongle.auth.domain.repository.RefreshTokenRepository;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.presentation.dto.ReissueTokenRequest;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenReissueService {

    private final TokenManager tokenManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberFinder memberFinder;

    public TokenInfo reissueTokens(ReissueTokenRequest reissueTokenRequest) {
        String refreshToken = reissueTokenRequest.refreshToken();
        if (!tokenManager.validateToken(refreshToken)
            || !refreshTokenRepository.existsByRefreshToken(refreshToken)) {
            throw new ApplicationException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = tokenManager.getUserId(refreshToken);
        Member member = memberFinder.getMemberOrThrow(userId);

        TokenInfo tokenInfo = tokenManager.generateTokens(member.getMemberId(),
            member.getMemberRole());

        RefreshToken issuedRefreshToken = RefreshToken.of(
            member.getMemberId(),
            tokenInfo.refreshToken(),
            tokenInfo.refreshTokenExpirationMillis()
        );
        refreshTokenRepository.save(issuedRefreshToken);

        return tokenInfo;
    }

}
