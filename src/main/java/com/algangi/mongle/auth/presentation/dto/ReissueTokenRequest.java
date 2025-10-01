package com.algangi.mongle.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenRequest(
    @NotBlank
    String refreshToken
) {

}
