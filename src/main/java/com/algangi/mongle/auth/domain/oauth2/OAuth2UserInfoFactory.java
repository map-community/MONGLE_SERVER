package com.algangi.mongle.auth.domain.oauth2;

import java.util.Map;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo createOAuth2UserInfo(String registrationId,
        Map<String, Object> attributes) {
        OAuth2Provider provider = OAuth2Provider.from(registrationId);
        if (provider == OAuth2Provider.KAKAO) {
            return new KakaoUserInfo(attributes);
        }
        throw new ApplicationException(AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER)
            .addErrorInfo("registrationId", registrationId);
    }
}
