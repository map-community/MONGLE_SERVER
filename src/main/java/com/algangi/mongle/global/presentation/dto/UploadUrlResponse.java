package com.algangi.mongle.global.presentation.dto;

import java.util.List;

public record UploadUrlResponse(
    List<IssuedUrlInfo> issuedUrlInfos
) {

    public static UploadUrlResponse of(List<IssuedUrlInfo> issuedUrlInfos) {
        return new UploadUrlResponse(issuedUrlInfos);
    }

}
