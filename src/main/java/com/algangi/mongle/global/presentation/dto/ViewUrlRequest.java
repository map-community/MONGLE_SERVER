package com.algangi.mongle.global.presentation.dto;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

public record ViewUrlRequest(
    @NotNull
    Set<String> s3KeySet
) {

}
