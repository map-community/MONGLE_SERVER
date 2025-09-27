package com.algangi.mongle.post.presentation.dto;

import java.util.List;

import com.algangi.mongle.post.application.dto.IssuedUrlInfo;

public record ViewUrlResponse(
    List<IssuedUrlInfo> issuedUrls
) {

    public static ViewUrlResponse of(List<IssuedUrlInfo> issuedUrlInfos) {
        return new ViewUrlResponse(issuedUrlInfos);
    }
}
