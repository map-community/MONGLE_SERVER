package com.algangi.mongle.auth.exception;

import org.springframework.http.HttpStatus;

import com.algangi.mongle.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증정보가 유효하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH-002", "요청에 대한 권한이 없습니다."),

    KAKAO_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "AUTH-005", "카카오 API 클라이언트 오류"),
    KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-006", "카카오 API 서버 오류"),
    KAKAO_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-007", "카카오 액세스 토큰 응답이 비어있습니다."),
    UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-008", "지원하지 않는 Oauth 인증 제공자입니다."),
    NOT_LINKED_ACCOUNT(HttpStatus.BAD_REQUEST, "AUTH-009", "연동되지 않은 소셜 계정입니다"),

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH-010", "이미 사용중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "AUTH-011", "이미 사용중인 닉네임입니다."),
    DUPLICATE_SOCIAL_ACCOUNT(HttpStatus.BAD_REQUEST, "AUTH-012", "이미 연동된 상태입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-015", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-016", "만료된 토큰입니다."),

    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-020", "이메일 인증 코드가 일치하지 않습니다"),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH-021", "이멩ㄹ 인증 코드가 만료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
