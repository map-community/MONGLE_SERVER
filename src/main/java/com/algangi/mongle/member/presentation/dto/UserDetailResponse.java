package com.algangi.mongle.member.presentation.dto;

public record UserDetailResponse(
    String nickname,
    String email,
    String profileImageUrl
) {

    public static UserDetailResponse of(String nickname, String email, String profileImageUrl) {
        return new UserDetailResponse(nickname, email, profileImageUrl);
    }

}
