package com.algangi.mongle.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AwsErrorCode implements ErrorCode {

    CLOUDFRONT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWS-001", "CloudFront 에러");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
