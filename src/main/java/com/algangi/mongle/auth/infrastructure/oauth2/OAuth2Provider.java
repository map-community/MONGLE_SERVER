package com.algangi.mongle.auth.infrastructure.oauth2;

import java.util.Arrays;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuth2Provider {

    KAKAO("kakao");

    private final String registrationId;

    public static OAuth2Provider from(String registrationId) {
        return Arrays.stream(values())
            .filter(provider -> provider.getRegistrationId().equalsIgnoreCase(registrationId))
            .findFirst()
            .orElseThrow(
                () -> new ApplicationException(AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER)
                    .addErrorInfo("registrationId", registrationId)
            );
    }
}

