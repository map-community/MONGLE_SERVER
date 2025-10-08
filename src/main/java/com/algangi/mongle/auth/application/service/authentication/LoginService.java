package com.algangi.mongle.auth.application.service.authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AuthTokenManager authTokenManager;

    @Transactional
    public TokenInfo login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new ApplicationException(AuthErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), member.getEncodedPassword())) {
            throw new ApplicationException(AuthErrorCode.UNAUTHORIZED);
        }

        return authTokenManager.generateTokens(member.getMemberId(),
            member.getMemberRole());
    }
}
