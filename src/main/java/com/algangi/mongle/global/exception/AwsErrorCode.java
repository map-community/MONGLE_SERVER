package com.algangi.mongle.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AwsErrorCode implements ErrorCode {

    CLOUDFRONT_PRESIGNED_URL_ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CLOUDFRONT-001",
        "CloudFront Presigned URL 발급에 실패했습니다."),

    S3_PRESIGNED_URL_ISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-001",
        "S3 Presigned URL 발급에 실패했습니다."),
    S3_FILE_COPY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-002", "S3 파일 복사에 실패했습니다."),
    S3_FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-003", "S3 파일 삭제에 실패했습니다."),
    S3_FILE_NOT_FOUND_IN_STORAGE(HttpStatus.NOT_FOUND, "S3-004", "스토리지에서 해당 파일을 찾을 수 없습니다."),
    S3_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3-005", "S3 알 수 없는 에러가 발생하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
