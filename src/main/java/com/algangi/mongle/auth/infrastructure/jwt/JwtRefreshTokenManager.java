package com.algangi.mongle.auth.infrastructure.jwt;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.application.service.RefreshTokenManager;
import com.algangi.mongle.auth.domain.model.RefreshToken;
import com.algangi.mongle.auth.domain.repository.RefreshTokenRepository;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;

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
    public RefreshToken generate(Long memberId) {
        String token = jwtHandler.createToken(memberId.toString(), Collections.emptyMap(),
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
    public Long getUserId(String token) {
        Claims claims = jwtHandler.parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }
}
