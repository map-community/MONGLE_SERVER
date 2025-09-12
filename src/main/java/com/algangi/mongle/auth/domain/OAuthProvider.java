package com.algangi.mongle.auth.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {

    KAKAO("kakao");

    private final String registrationId;

    public static OAuthProvider from(String registrationId) {
        return Arrays.stream(values())
            .filter(provider -> provider.getRegistrationId().equalsIgnoreCase(registrationId))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId));
    }
}

