package com.algangi.mongle.comment.domain;

import com.querydsl.core.types.OrderSpecifier;
import static com.algangi.mongle.comment.domain.QComment.comment;

public enum CommentSort {
    LATEST, LIKES;

    public OrderSpecifier<?>[] getOrderSpecifiers() {
        return switch (this) {
            case LIKES -> new OrderSpecifier[]{
                    comment.likeCount.desc(),
                    comment.createdDate.desc(),
                    comment.id.desc()
            };
            case LATEST -> new OrderSpecifier[]{
                    comment.createdDate.desc(),
                    comment.id.desc()
            };
        };
    }
}
