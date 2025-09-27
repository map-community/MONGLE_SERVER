package com.algangi.mongle.comment.domain.model;

import java.time.LocalDateTime;

public interface CursorConvertible {
    Long getId();
    long getLikeCount();
    LocalDateTime getCreatedAt();
}