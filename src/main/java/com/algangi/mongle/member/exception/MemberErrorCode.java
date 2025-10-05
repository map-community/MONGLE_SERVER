package com.algangi.mongle.member.exception;

import com.algangi.mongle.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "회원을 찾을 수 없습니다."),
    MEMBER_IS_BANNED(HttpStatus.FORBIDDEN, "MEMBER-002", "활동이 정지된 회원입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
