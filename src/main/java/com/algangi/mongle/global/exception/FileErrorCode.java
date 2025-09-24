package com.algangi.mongle.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

    INVALID_FILE_COUNT(HttpStatus.BAD_REQUEST, "FILE-001", "이미지 파일의 총 용량이 최대 허용치를 초과했습니다."),
    INVALID_VIDEO_COUNT(HttpStatus.BAD_REQUEST, "FILE-002", "업로드할 수 있는 동영상의 최대 개수를 초과했습니다."),
    INVALID_TOTAL_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "FILE-003", "이미지 파일의 총 용량이 최대 허용치를 초과했습니다."),
    INVALID_VIDEO_SIZE(HttpStatus.BAD_REQUEST, "FILE-004", "업로드할 수 있는 동영상의 최대 사이즈를 초과했습니다."),
    INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "FILE-005", "업로드할 수 있는 이미지 사이즈를 초과했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;


}
