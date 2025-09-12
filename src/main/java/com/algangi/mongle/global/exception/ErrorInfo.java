package com.algangi.mongle.global.exception;

import java.util.Map;

public record ErrorInfo(
        Map<String, Object> errorInfo
) {

    public static ErrorInfo of(Map<String, Object> errorInfo) {
        return new ErrorInfo(errorInfo);
    }

}
