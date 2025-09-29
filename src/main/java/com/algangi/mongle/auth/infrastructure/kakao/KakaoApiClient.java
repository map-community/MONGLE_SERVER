package com.algangi.mongle.auth.infrastructure.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;


@Component
public class KakaoApiClient {

    private final RestClient restClient;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    public KakaoApiClient(RestClient.Builder builder) {
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
    }

    public String getAccessToken(String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri",
            redirectUri.replace("{baseUrl}", "http://localhost:8080"));
        body.add("code", authorizationCode);

        KakaoTokenResponse tokenResponse = restClient.post()
            .uri(tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .retrieve()
            .body(KakaoTokenResponse.class);

        return tokenResponse.accessToken();
    }

    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        return restClient.get()
            .uri(userInfoUri)
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(KakaoUserInfoResponse.class);
    }
}