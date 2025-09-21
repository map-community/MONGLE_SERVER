package com.algangi.mongle.comment.infrastructure.persistence.query;

import com.algangi.mongle.comment.domain.model.CommentSort;
import com.querydsl.core.types.OrderSpecifier;

import static com.algangi.mongle.comment.domain.model.QComment.comment;

public class CommentOrderSpecifiers {

    public static OrderSpecifier<?>[] of(CommentSort sort) {
        if (sort == null) sort = CommentSort.LATEST;

        return switch (sort) {
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
