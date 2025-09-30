package com.algangi.mongle.auth.infrastructure.kakao;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.algangi.mongle.auth.application.service.OAuth2UserInfoMapper;
import com.algangi.mongle.auth.domain.oauth2.OAuth2UserInfo;
import com.algangi.mongle.auth.infrastructure.kakao.dto.KakaoUserInfoResponse;

@Component
public class KakaoUserInfoMapper implements OAuth2UserInfoMapper<KakaoUserInfoResponse> {

    @Override
    public OAuth2UserInfo mapToUserInfo(KakaoUserInfoResponse response) {
        Map<String, Object> kakaoAccount = response.kakaoAccount();
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return new OAuth2UserInfo(
            response.providerId(),
            (String) kakaoAccount.get("email"),
            (String) profile.get("nickname"),
            (String) profile.get("profile_image_url"),
            null
        );
    }
}

