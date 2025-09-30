package com.algangi.mongle.auth.application.service;

import com.algangi.mongle.auth.domain.oauth2.OAuth2Provider;
import com.algangi.mongle.auth.domain.oauth2.OAuth2UserInfo;

public interface OAuth2Client {

    OAuth2Provider getProvider();

    String exchangeCodeForAccessToken(String authorizationCode);

    OAuth2UserInfo fetchUserInfo(String accessToken);

    default OAuth2UserInfo fetchUserInfoWithAuthorizationCode(String authorizationCode) {
        String token = exchangeCodeForAccessToken(authorizationCode);
        return fetchUserInfo(token);
    }
}
