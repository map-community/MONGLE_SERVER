package com.algangi.mongle.auth.exception;

import org.springframework.http.HttpStatus;

import com.algangi.mongle.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증정보가 유효하지 않습니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH-002", "요청에 대한 권한이 없습니다."),

    KAKAO_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "AUTH-003", "카카오 API 클라이언트 오류"),
    KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-004", "카카오 API 서버 오류"),
    UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-005", "지원하지 않는 Oauth 인증 제공자입니다."),
    NOT_LINKED_ACCOUNT(HttpStatus.BAD_REQUEST, "AUTH-006", "연동되지 않은 소셜 계정입니다"),

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH-007", "이미 사용중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "AUTH-008", "이미 사용중인 닉네임입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH-008", "유효하지 않은 리프레쉬 토큰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
