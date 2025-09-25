package com.algangi.mongle.global.presentation.dto;

import java.util.List;

public record UploadUrlResponse(
    List<IssuedUrlInfo> issuedUrls
) {

    public static UploadUrlResponse of(List<IssuedUrlInfo> issuedUrls) {
        return new UploadUrlResponse(issuedUrls);
    }

}
