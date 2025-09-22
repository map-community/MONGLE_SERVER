package com.algangi.mongle.comment.infrastructure.persistence.vo;

import java.util.List;

public record PaginationResult<T>(
        List<T> content,
        boolean hasNext
) {

    public static <T> PaginationResult<T> of(List<T> source, int pageSize) {
        boolean hasNext = source.size() > pageSize;
        List<T> content = hasNext
                ? source.subList(0, pageSize)
                : source;

        return new PaginationResult<>(content, hasNext);
    }
}