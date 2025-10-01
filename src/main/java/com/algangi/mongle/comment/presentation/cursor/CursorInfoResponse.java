package com.algangi.mongle.comment.presentation.cursor;

import java.util.Collections;
import java.util.List;

public record CursorInfoResponse<T>(
        List<T> comments,
        String nextCursor,
        boolean hasNext
) {

    public CursorInfoResponse(List<T> comments, String nextCursor, boolean hasNext) {
        this.comments = List.copyOf(comments);
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static <T> CursorInfoResponse<T> of(List<T> comments, String nextCursor, boolean hasNext) {
        return new CursorInfoResponse<>(comments, nextCursor, hasNext);
    }

    public static <T> CursorInfoResponse<T> empty() {
        return new CursorInfoResponse<>(Collections.emptyList(), null, false);
    }
}