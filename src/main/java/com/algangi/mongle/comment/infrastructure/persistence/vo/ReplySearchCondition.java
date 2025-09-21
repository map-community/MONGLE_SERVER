package com.algangi.mongle.comment.infrastructure.persistence.vo;

import com.algangi.mongle.comment.domain.model.CommentSort;

public record ReplySearchCondition(
        Long parentId,
        String cursor,
        CommentSort sort
) {

}