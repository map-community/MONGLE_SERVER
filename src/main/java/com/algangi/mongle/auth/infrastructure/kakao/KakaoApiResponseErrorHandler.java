package com.algangi.mongle.auth.infrastructure.kakao;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import com.algangi.mongle.auth.exception.AuthErrorCode;
import com.algangi.mongle.auth.infrastructure.kakao.dto.KakaoErrorResponse;
import com.algangi.mongle.global.exception.ApplicationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoApiResponseErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response)
        throws IOException {
        AuthErrorCode errorCode = response.getStatusCode().is4xxClientError() ?
            AuthErrorCode.KAKAO_CLIENT_ERROR : AuthErrorCode.KAKAO_SERVER_ERROR;
        String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            KakaoErrorResponse kakaoError = objectMapper.readValue(errorBody,
                KakaoErrorResponse.class);

            log.error(
                "카카오 API 클라이언트 오류 발생. method={}, url={}, status={}, code={}, message={}",
                method,
                url,
                response.getStatusCode(),
                kakaoError.errorCode(),
                kakaoError.errorDescription()
            );

            throw new ApplicationException(errorCode)
                .addErrorInfo("kakao_error_code", kakaoError.errorCode())
                .addErrorInfo("kakao_error_description", kakaoError.errorDescription());

        } catch (JsonProcessingException e) {
            log.error(
                "카카오 API 클라이언트 오류 발생. method={}, url={}, status={}, body={}",
                method,
                url,
                response.getStatusCode(),
                errorBody
            );

            throw new ApplicationException(errorCode)
                .addErrorInfo("response_body", errorBody);
        }
    }
}