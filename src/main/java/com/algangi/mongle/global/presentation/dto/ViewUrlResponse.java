package com.algangi.mongle.global.presentation.dto;

import java.util.List;

public record ViewUrlResponse(
    List<IssuedUrlInfo> issuedUrls
) {

    public static ViewUrlResponse of(List<IssuedUrlInfo> issuedUrlInfos) {
        return new ViewUrlResponse(issuedUrlInfos);
    }
}
