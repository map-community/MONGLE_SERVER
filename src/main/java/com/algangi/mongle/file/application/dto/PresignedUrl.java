package com.algangi.mongle.file.application.dto;

import java.time.Instant;

public record PresignedUrl(
    String fileKey,
    String url,
    Instant expiresAt
) {

}
