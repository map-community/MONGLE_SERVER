package com.algangi.mongle.post.presentation.dto;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

public record ViewUrlRequest(
    @NotNull
    Set<String> s3KeySet
) {

}
