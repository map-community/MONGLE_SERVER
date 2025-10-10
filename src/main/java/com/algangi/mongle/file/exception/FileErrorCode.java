package com.algangi.mongle.file.exception;

import org.springframework.http.HttpStatus;

import com.algangi.mongle.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {

    INVALID_FILE_COUNT(HttpStatus.BAD_REQUEST, "FILE-001", "업로드할 수 있는 파일 개수를 초과했습니다."),
    INVALID_VIDEO_COUNT(HttpStatus.BAD_REQUEST, "FILE-002", "업로드할 수 있는 동영상의 최대 개수를 초과했습니다."),
    INVALID_TOTAL_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "FILE-003", "이미지 파일의 총 용량이 최대 허용치를 초과했습니다."),
    INVALID_VIDEO_SIZE(HttpStatus.BAD_REQUEST, "FILE-004", "업로드할 수 있는 동영상의 최대 사이즈를 초과했습니다."),
    INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "FILE-005", "업로드할 수 있는 이미지 사이즈를 초과했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE-006",
        "지원하지 않는 파일 유형입니다. (PostFile, ProfileImage만 허용)"),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FILE-007", "파일 확장자가 유효하지 않습니다."),
    INVALID_TEMPORARY_KEY(HttpStatus.BAD_REQUEST, "FILE-008", "유효하지 않은 임시 파일 경로입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
