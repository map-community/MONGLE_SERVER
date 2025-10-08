package com.algangi.mongle.auth.infrastructure.jwt;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.application.service.authentication.RefreshTokenManager;
import com.algangi.mongle.auth.domain.model.RefreshToken;
import com.algangi.mongle.auth.domain.repository.RefreshTokenRepository;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;
import com.querydsl.core.util.StringUtils;

import io.jsonwebtoken.Claims;

@Service
public class JwtRefreshTokenManager implements RefreshTokenManager {

    private final JwtHandler jwtHandler;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationMillis;

    public JwtRefreshTokenManager(JwtHandler jwtHandler,
        RefreshTokenRepository refreshTokenRepository,
        JwtProperties jwtProperties) {
        this.jwtHandler = jwtHandler;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMillis = jwtProperties.refreshTokenExpirationMillis();
    }

    @Override
    public RefreshToken generate(String memberId) {
        if (StringUtils.isNullOrEmpty(memberId)) {
            throw new IllegalArgumentException("리프레쉬 토큰 생성 시 회원 ID는 필수값입니다.");
        }
        String token = jwtHandler.createToken(memberId, Collections.emptyMap(),
            refreshTokenExpirationMillis);
        RefreshToken refreshToken = RefreshToken.of(memberId, token, refreshTokenExpirationMillis);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void validateToken(String token) {
        jwtHandler.validateSignature(token);
        if (!refreshTokenRepository.existsByRefreshToken(token)) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    public String getUserId(String token) {
        Claims claims = jwtHandler.parseClaims(token);
        return claims.getSubject();
    }
}
