package com.algangi.mongle.comment.domain.model;

import java.time.Instant;

public interface CursorConvertible {
    String getId();
    long getLikeCount();
    Instant getCreatedAt();
}