package com.algangi.mongle.post.exception;

import org.springframework.http.HttpStatus;

import com.algangi.mongle.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-001", "게시글이 존재하지 않습니다."),
    CLOUD_NOT_FOUND(HttpStatus.NOT_FOUND, "POST-002", "요청한 구름이 존재하지 않습니다.");

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
