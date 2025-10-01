package com.algangi.mongle.auth.infrastructure.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
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


@Component
public class KakaoApiClient implements OAuth2Client {

    private final RestClient restClient;
    private final KakaoUserInfoMapper kakaoUserInfoMapper;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    public KakaoApiClient(RestClient.Builder builder, KakaoUserInfoMapper kakaoUserInfoMapper) {
        this.restClient = builder
            .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                throw new ApplicationException(AuthErrorCode.KAKAO_CLIENT_ERROR)
                    .addErrorInfo("statusText", response.getStatusText());
            })
            .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                throw new ApplicationException(AuthErrorCode.KAKAO_SERVER_ERROR)
                    .addErrorInfo("statusText", response.getStatusText());
            })
            .build();
        this.kakaoUserInfoMapper = kakaoUserInfoMapper;
    }

    @Override
    public String exchangeCodeForAccessToken(String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);

        KakaoTokenResponse tokenResponse = restClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .body(KakaoTokenResponse.class);

        return tokenResponse.accessToken();
    }

    @Override
    public OAuth2UserInfo fetchUserInfo(String accessToken) {
        KakaoUserInfoResponse kakaoResponse = restClient.get()
            .uri(userInfoUri)
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(KakaoUserInfoResponse.class);

        return kakaoUserInfoMapper.mapToUserInfo(kakaoResponse);
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.KAKAO;
    }
}