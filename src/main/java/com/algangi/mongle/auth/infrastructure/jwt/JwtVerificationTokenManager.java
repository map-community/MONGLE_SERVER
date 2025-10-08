package com.algangi.mongle.auth.infrastructure.jwt;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.algangi.mongle.auth.application.service.email.VerificationTokenManager;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;
import com.querydsl.core.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtVerificationTokenManager implements VerificationTokenManager {

    private static final String CLAIM_KEY_PURPOSE = "purpose";
    private static final String VALUE_EMAIL_VERIFICATION = "email_verification";
    private static final long VERIFICATION_TOKEN_EXPIRATION_MS = 60 * 60 * 1000L;
    private final JwtHandler jwtHandler;

    @Override
    public String generate(String email) {
        if (StringUtils.isNullOrEmpty(email)) {
            throw new IllegalArgumentException("이메일 인증용 토큰 발급을 위해 이메일은 필수값입니다.");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_PURPOSE, VALUE_EMAIL_VERIFICATION);
        return jwtHandler.createToken(email, claims, VERIFICATION_TOKEN_EXPIRATION_MS);
    }

    @Override
    public void validateToken(String token, String email) {
        jwtHandler.validateSignature(token);
        if (!jwtHandler.parseClaims(token).getSubject().equals(email) ||
            !jwtHandler.parseClaims(token).get(CLAIM_KEY_PURPOSE)
                .equals(VALUE_EMAIL_VERIFICATION)) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
    }

}
