package com.algangi.mongle.comment.infrastructure.persistence.vo;

import com.algangi.mongle.comment.domain.model.CommentSort;

public record CommentSearchCondition(
        Long postId,
        String cursor,
        CommentSort sort
) {

}