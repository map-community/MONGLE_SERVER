package com.algangi.mongle.post.presentation.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record UploadUrlRequest(
    @NotNull(message = "파일 목록은 필수입니다")
    @Valid
    List<UploadFileInfo> files
) {

    public record UploadFileInfo(
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,

        @NotNull(message = "파일 크기는 필수입니다")
        @Min(value = 1, message = "파일 크기는 1바이트 이상이어야 합니다")
        Long fileSize
    ) {

    }
}
