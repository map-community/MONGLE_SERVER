package com.algangi.mongle.auth.application.service.oauth;

import com.algangi.mongle.auth.domain.oauth2.OAuth2UserInfo;

public interface OAuth2UserInfoMapper<T> {

    OAuth2UserInfo mapToUserInfo(T attributes);
}
