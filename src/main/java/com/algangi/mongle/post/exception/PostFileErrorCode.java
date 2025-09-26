package com.algangi.mongle.post.exception;

import org.springframework.http.HttpStatus;

import com.algangi.mongle.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostFileErrorCode implements ErrorCode {

    INVALID_FILE_COUNT(HttpStatus.BAD_REQUEST, "FILE-001", "이미지 파일의 총 용량이 최대 허용치를 초과했습니다."),
    INVALID_VIDEO_COUNT(HttpStatus.BAD_REQUEST, "FILE-002", "업로드할 수 있는 동영상의 최대 개수를 초과했습니다."),
    INVALID_TOTAL_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "FILE-003", "이미지 파일의 총 용량이 최대 허용치를 초과했습니다."),
    INVALID_VIDEO_SIZE(HttpStatus.BAD_REQUEST, "FILE-004", "업로드할 수 있는 동영상의 최대 사이즈를 초과했습니다."),
    INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "FILE-005", "업로드할 수 있는 이미지 사이즈를 초과했습니다."),

    FILE_NOT_FOUND_STORAGE(HttpStatus.NOT_FOUND, "FILE-006", "파일 저장소에 해당 파일이 존재하지 않습니다."),
    FILE_NOT_FOUND_DB(HttpStatus.NOT_FOUND, "FILE-007", "DB에 해당 파일의 메타데이터가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;


}
