package com.algangi.mongle.file.application.dto;

import java.time.LocalDateTime;

public record PresignedUrl(
    String fileKey,
    String url,
    LocalDateTime expiresAt
) {

}
