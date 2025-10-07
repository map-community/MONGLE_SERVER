package com.algangi.mongle.member.presentation.dto;

public record UserDetailResponse(
    String nickname,
    String email,
    String profileImageUrl,
    SocialLinkStatus socialLinkStatus
) {

    public static UserDetailResponse of(String nickname, String email, String profileImageUrl,
        SocialLinkStatus socialLinkStatus) {
        return new UserDetailResponse(nickname, email, profileImageUrl, socialLinkStatus);
    }

}
