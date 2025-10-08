package com.algangi.mongle.auth.presentation.dto;

import com.algangi.mongle.global.annotation.ValidEmail;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
    @ValidEmail
    String email,
    @NotBlank(message = "인증 코드는 필수값입니다.")
    String verificationCode
) {

}
