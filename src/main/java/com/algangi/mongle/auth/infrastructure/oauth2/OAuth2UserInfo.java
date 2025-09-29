package com.algangi.mongle.auth.infrastructure.oauth2;

public interface OAuth2UserInfo {

    String getProvider();

    String getProviderId();

    String getNickname();

    String getEmail();

    String getProfileImageUrl();
}
