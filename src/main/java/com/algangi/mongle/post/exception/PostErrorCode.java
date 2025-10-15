package com.algangi.mongle.post.exception;

import org.springframework.http.HttpStatus;

import com.algangi.mongle.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-001", "게시글이 존재하지 않습니다."),
    CLOUD_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-002", "요청한 구름이 존재하지 않습니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "POST-003", "게시글에 대한 권한이 없습니다."),
    POST_RATE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POST-004", "게시글은 3분마다 최대 하나씩 생성 가능합니다."),
    DUPLICATE_POST_IN_CELL(HttpStatus.CONFLICT, "POST-005", "게시글은 셀 당 하나만 생성 가능합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
