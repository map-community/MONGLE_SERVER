package com.algangi.mongle.auth.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.domain.model.RefreshToken;
import com.algangi.mongle.auth.domain.repository.RefreshTokenRepository;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.presentation.dto.LoginRequest;
import com.algangi.mongle.auth.presentation.dto.TokenInfo;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenManager tokenManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenInfo login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new ApplicationException(AuthErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ApplicationException(AuthErrorCode.UNAUTHORIZED);
        }

        TokenInfo tokenInfo = tokenManager.generateTokens(member.getMemberId(),
            member.getMemberRole());

        RefreshToken refreshToken = RefreshToken.of(
            member.getMemberId(),
            tokenInfo.refreshToken(),
            tokenInfo.refreshTokenExpirationMillis()
        );
        refreshTokenRepository.save(refreshToken);

        return tokenInfo;
    }
}
