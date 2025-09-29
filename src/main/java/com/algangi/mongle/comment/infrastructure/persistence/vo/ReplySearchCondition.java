package com.algangi.mongle.comment.infrastructure.persistence.vo;

import com.algangi.mongle.comment.domain.model.CommentSort;

public record ReplySearchCondition(
        String parentId,
        String cursor,
        CommentSort sort
) {

}