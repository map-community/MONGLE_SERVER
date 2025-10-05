package com.algangi.mongle.auth.infrastructure.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoErrorResponse(
    String error,
    @JsonProperty("error_description") String errorDescription,
    @JsonProperty("error_code") String errorCode
) {

}