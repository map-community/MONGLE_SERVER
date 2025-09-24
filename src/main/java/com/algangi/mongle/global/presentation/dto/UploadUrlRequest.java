package com.algangi.mongle.global.presentation.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public record UploadUrlRequest(
    @NotNull(message = "파일 목록은 필수입니다")
    @Valid
    List<UploadFileInfo> files
) {

}
