package com.algangi.mongle.comment.exception;

import com.algangi.mongle.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {

    REPLY_TO_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COMMENT-001", "대댓글에는 답글을 작성할 수 없습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMENT-002", "댓글에 대한 권한이 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT-003", "댓글을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}