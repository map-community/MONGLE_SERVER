package com.algangi.mongle.auth.presentation.dto;

import com.algangi.mongle.global.annotation.ValidEmail;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @ValidEmail
    String email,
    @NotBlank(message = "비밀번호는 필수값입니다.")
    String password
) {

}
