package com.algangi.mongle.auth.infrastructure.kakao.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
    @JsonProperty("id") String providerId,
    @JsonProperty("kakao_account") Map<String, Object> kakaoAccount
) {

}
