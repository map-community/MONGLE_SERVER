package com.algangi.mongle.global.exception;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class ApplicationException extends RuntimeException {

    ErrorCode errorCode;
    Map<String, Object> errorInfo;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errorInfo = new HashMap<>();
        log.error("Application Exception 발생: status={}, code={}, message={}", errorCode.getStatus(),
            errorCode.getCode(), errorCode.getMessage(), this);
    }

    public ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.errorInfo = new HashMap<>();
        log.error("Application Exception 발생: status={}, code={}, message={}", errorCode.getStatus(),
            errorCode.getCode(), errorCode.getMessage(), this);
    }

    public ApplicationException addErrorInfo(String key, Object value) {
        this.errorInfo.put(key, value);
        return this;
    }
}
