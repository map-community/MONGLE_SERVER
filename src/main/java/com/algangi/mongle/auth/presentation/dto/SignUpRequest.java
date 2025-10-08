package com.algangi.mongle.auth.presentation.dto;

import com.algangi.mongle.global.annotation.ValidEmail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @ValidEmail
    String email,
    @NotBlank(message = "비밀번호는 필수값입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    String password,
    @NotBlank(message = "닉네임은 필수값입니다.")
    String nickname,
    String profileImageKey,
    @NotBlank(message = "이메일 인증용 토큰은 필수값입니다.")
    String verificationToken
) {

}
