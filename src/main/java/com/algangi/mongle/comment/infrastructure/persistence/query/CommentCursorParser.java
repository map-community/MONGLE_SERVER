package com.algangi.mongle.comment.infrastructure.persistence.query;

import com.algangi.mongle.comment.domain.model.CommentSort;
import com.algangi.mongle.global.util.ParsingUtil;
import com.querydsl.core.types.dsl.BooleanExpression;

import static com.algangi.mongle.comment.domain.model.QComment.comment;

public class CommentCursorParser {

    public static BooleanExpression parse(String cursor, CommentSort sort) {
        if (cursor == null || cursor.isBlank()) return null;

        String[] parts = cursor.split("_");
        return switch (sort) {
            case LATEST -> latestCondition(parts);
            case LIKES -> likesCondition(parts);
        };
    }

    private static BooleanExpression latestCondition(String[] parts) {
        if (parts.length != 2)
            throw new IllegalArgumentException("잘못된 커서 형식: " + String.join("_", parts));

        var created = ParsingUtil.parseDate(parts[0]);
        var id = ParsingUtil.parseLong(parts[1]);

        return comment.createdDate.lt(created).or(comment.createdDate.eq(created).and(comment.id.lt(id)));
    }

    private static BooleanExpression likesCondition(String[] parts) {
        if (parts.length != 3) throw new IllegalArgumentException("잘못된 커서 형식: " + String.join("_", parts));

        var like = ParsingUtil.parseLong(parts[0]);
        var created = ParsingUtil.parseDate(parts[1]);
        var id = ParsingUtil.parseLong(parts[2]);

        return comment.likeCount.lt(like)
                .or(comment.likeCount.eq(like).and(comment.createdDate.lt(created)))
                .or(comment.likeCount.eq(like).and(comment.createdDate.eq(created)).and(comment.id.lt(id)));
    }
}