package com.algangi.mongle.file.presentation.dto;

import java.util.List;

import com.algangi.mongle.file.application.dto.PresignedUrl;

public record UploadUrlResponse(
    List<PresignedUrl> issuedUrls
) {

    public static UploadUrlResponse of(List<PresignedUrl> issuedUrls) {
        return new UploadUrlResponse(issuedUrls);
    }

}
