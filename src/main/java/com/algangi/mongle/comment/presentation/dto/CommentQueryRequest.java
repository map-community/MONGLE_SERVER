package com.algangi.mongle.comment.presentation.dto;

import com.algangi.mongle.comment.domain.model.CommentSort;

public record CommentQueryRequest(
        String cursor,
        Integer size,
        CommentSort sort,
        String memberId
) {

    private static final int DEFAULT_SIZE = 10;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;

    public CommentQueryRequest {
        cursor = resolveCursor(cursor);
        size = resolveSize(size);
        sort = resolveSort(sort);
    }

    private String resolveCursor(String cursor) {
        return cursor;
    }

    private int resolveSize(Integer size) {
        int resolved = (size == null) ? DEFAULT_SIZE : size;
        return Math.min(Math.max(resolved, MIN_SIZE), MAX_SIZE);
    }

    private CommentSort resolveSort(CommentSort sort) {
        return (sort == null) ? CommentSort.LIKES : sort;
    }
}
