package com.algangi.mongle.post.presentation.dto;

import java.util.List;

import com.algangi.mongle.post.application.dto.IssuedUrlInfo;

public record UploadUrlResponse(
    List<IssuedUrlInfo> issuedUrls
) {

    public static UploadUrlResponse of(List<IssuedUrlInfo> issuedUrls) {
        return new UploadUrlResponse(issuedUrls);
    }

}
