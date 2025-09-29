package com.algangi.mongle.comment.domain.model;

import java.time.LocalDateTime;

public interface CursorConvertible {
    String getId();
    long getLikeCount();
    LocalDateTime getCreatedAt();
}