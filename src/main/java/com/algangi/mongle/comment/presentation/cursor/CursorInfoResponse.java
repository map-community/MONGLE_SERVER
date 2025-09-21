package com.algangi.mongle.comment.presentation.cursor;

import java.util.Collections;
import java.util.List;

public record CursorInfoResponse<T>(
        List<T> values,
        String nextCursor,
        boolean hasNext
) {

    public CursorInfoResponse(List<T> values, String nextCursor, boolean hasNext) {
        this.values = List.copyOf(values);
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static <T> CursorInfoResponse<T> of(List<T> values, String nextCursor) {
        boolean hasNext = nextCursor != null && !nextCursor.isBlank();
        return new CursorInfoResponse<>(values, nextCursor, hasNext);
    }

    public static <T> CursorInfoResponse<T> empty() {
        return new CursorInfoResponse<>(Collections.emptyList(), null, false);
    }
}
