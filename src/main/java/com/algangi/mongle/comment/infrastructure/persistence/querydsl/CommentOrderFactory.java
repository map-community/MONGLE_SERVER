package com.algangi.mongle.comment.infrastructure.persistence.querydsl;

import com.algangi.mongle.comment.domain.model.CommentSort;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.stereotype.Component;

import static com.algangi.mongle.comment.domain.model.QComment.comment;

@Component
public class CommentOrderFactory {

    public OrderSpecifier<?>[] createOrderSpecifiers(CommentSort sort) {
        CommentSort finalSort =
                (sort == null) ? CommentSort.LIKES : sort;

        return switch (finalSort) {
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