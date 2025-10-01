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
        if (kakaoAccount == null) {
            throw new IllegalStateException("카카오 계정 정보가 비어 있습니다.");
        }
        Object profileObj = kakaoAccount.get("profile");
        Map<String, Object> profile =
            profileObj instanceof Map<?, ?> casted ? (Map<String, Object>) casted : Map.of();

        return new OAuth2UserInfo(
            response.providerId(),
            (String) kakaoAccount.get("email"),
            (String) profile.get("nickname"),
            (String) profile.get("profile_image_url"),
            null
        );
    }
}

