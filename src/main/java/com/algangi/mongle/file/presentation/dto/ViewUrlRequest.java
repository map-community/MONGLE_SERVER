package com.algangi.mongle.file.presentation.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record ViewUrlRequest(
    @NotNull
    List<String> fileKeyList
) {

}
