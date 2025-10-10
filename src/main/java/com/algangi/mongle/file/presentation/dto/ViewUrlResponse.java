package com.algangi.mongle.file.presentation.dto;

import java.util.List;

import com.algangi.mongle.file.application.dto.PresignedUrl;

public record ViewUrlResponse(
    List<PresignedUrl> issuedUrls
) {

    public static ViewUrlResponse of(List<PresignedUrl> presignedUrls) {
        return new ViewUrlResponse(presignedUrls);
    }
}
