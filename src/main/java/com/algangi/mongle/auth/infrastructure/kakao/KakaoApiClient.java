package com.algangi.mongle.auth.infrastructure.kakao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.algangi.mongle.auth.application.service.OAuth2Client;
import com.algangi.mongle.auth.domain.oauth2.OAuth2Provider;
import com.algangi.mongle.auth.domain.oauth2.OAuth2UserInfo;
import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.infrastructure.kakao.dto.KakaoTokenResponse;
import com.algangi.mongle.auth.infrastructure.kakao.dto.KakaoUserInfoResponse;
import com.algangi.mongle.global.exception.ApplicationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class KakaoApiClient implements OAuth2Client {

    private final RestClient restClient;
    private final KakaoUserInfoMapper kakaoUserInfoMapper;
    private final ObjectMapper objectMapper;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    public KakaoApiClient(RestClient.Builder builder, KakaoApiResponseErrorHandler errorHandler,
        KakaoUserInfoMapper kakaoUserInfoMapper,
        ObjectMapper objectMapper) {
        this.restClient = builder
            .defaultStatusHandler(errorHandler)
            .build();
        this.kakaoUserInfoMapper = kakaoUserInfoMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public String exchangeCodeForAccessToken(String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);
        body.add("client_secret", clientSecret);

        KakaoTokenResponse tokenResponse = restClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .body(KakaoTokenResponse.class);

        return Optional.ofNullable(tokenResponse)
            .map(KakaoTokenResponse::accessToken)
            .orElseThrow(() -> new ApplicationException(AuthErrorCode.KAKAO_INVALID_RESPONSE));
    }

    @Override
    public OAuth2UserInfo fetchUserInfo(String accessToken) {
        KakaoUserInfoResponse userInfoResponse = restClient.get()
            .uri(userInfoUri)
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(KakaoUserInfoResponse.class);

        return Optional.ofNullable(userInfoResponse)
            .map(kakaoUserInfoMapper::mapToUserInfo)
            .orElseThrow(() -> new ApplicationException(AuthErrorCode.KAKAO_INVALID_RESPONSE));
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.KAKAO;
    }
}