package com.algangi.mongle.file.application.dto;

import java.time.LocalDateTime;

public record IssuedUrlInfo(
    String fileKey,
    String presignedUrl,
    LocalDateTime expiresAt
) {

}
