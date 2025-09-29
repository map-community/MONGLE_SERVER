package com.algangi.mongle.auth.infrastructure.kakao;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
    @JsonProperty("id") String providerId,
    @JsonProperty("kakao_account") Map<String, Object> kakaoAccount
) {

    public String getNickname() {
        if (kakaoAccount != null && kakaoAccount.containsKey("profile")) {
            Map<String, String> profile = (Map<String, String>) kakaoAccount.get("profile");
            return profile.get("nickname");
        }
        return null;
    }
}
