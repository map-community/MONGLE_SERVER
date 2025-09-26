package com.algangi.mongle.post.application.dto;

import java.time.LocalDateTime;

public record IssuedUrlInfo(
    String fileKey,
    String presignedUrl,
    LocalDateTime expiresAt
) {

}
