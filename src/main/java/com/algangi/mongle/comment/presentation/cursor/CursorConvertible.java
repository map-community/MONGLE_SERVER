package com.algangi.mongle.comment.presentation.cursor;

import java.time.LocalDateTime;

public interface CursorConvertible {
    Long getId();
    long getLikeCount();
    LocalDateTime getCreatedAt();
}