package com.algangi.mongle.global.exception;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

    ErrorCode errorCode;
    Map<String, Object> errorInfo;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.errorInfo = new HashMap<>();
    }

    public ApplicationException addErrorInfo(String key, Object value) {
        this.errorInfo.put(key, value);
        return this;
    }
}
