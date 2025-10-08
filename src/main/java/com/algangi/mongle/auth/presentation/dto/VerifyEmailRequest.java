package com.algangi.mongle.auth.presentation.dto;

import com.algangi.mongle.global.annotation.ValidEmail;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
    @ValidEmail
    String email,
    @NotBlank
    String verificationCode
) {

}
