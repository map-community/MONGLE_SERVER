package com.algangi.mongle.comment.exception;

import com.algangi.mongle.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {

    ALREADY_DELETED(HttpStatus.BAD_REQUEST, "COMMENT-001", "이미 삭제된 댓글입니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMENT-002", "댓글에 대한 권한이 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT-003", "댓글을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}