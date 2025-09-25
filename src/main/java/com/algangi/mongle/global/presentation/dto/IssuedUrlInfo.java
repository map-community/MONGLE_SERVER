package com.algangi.mongle.global.presentation.dto;

import java.time.LocalDateTime;

public record IssuedUrlInfo(
    String fileKey,
    String presignedUrl,
    LocalDateTime expiresAt
) {

}
