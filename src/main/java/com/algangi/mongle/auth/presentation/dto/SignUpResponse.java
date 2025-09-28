package com.algangi.mongle.auth.presentation.dto;

import com.algangi.mongle.member.domain.Member;

public record SignUpResponse(
    Long memberId,
    String email,
    String nickname
) {

    public static SignUpResponse from(Member member) {
        return new SignUpResponse(
            member.getMemberId(),
            member.getEmail(),
            member.getNickname()
        );
    }

}
