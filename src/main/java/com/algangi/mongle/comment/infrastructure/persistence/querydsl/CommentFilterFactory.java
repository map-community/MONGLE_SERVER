package com.algangi.mongle.comment.infrastructure.persistence.querydsl;

import static com.algangi.mongle.comment.domain.model.QComment.comment;

import org.springframework.stereotype.Component;

import com.algangi.mongle.comment.domain.model.CommentSort;
import com.algangi.mongle.comment.domain.model.QComment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;

@Component
public class CommentFilterFactory {

    public BooleanExpression eqPostId(String postId) {
        if (postId == null) {
            return null;
        }
        return comment.post.id.eq(postId);
    }

    public BooleanExpression eqParentId(String parentId) {
        if (parentId == null) {
            return null;
        }
        return comment.parentComment.id.eq(parentId);
    }

    public BooleanExpression isParentComment() {
        return comment.parentComment.isNull();
    }

    public BooleanExpression cursorCondition(String cursor, CommentSort sort) {
        return CommentCursorParser.parse(cursor, sort);
    }

    public BooleanExpression visibleCommentCondition() {
        QComment reply = new QComment("reply");
        return comment.deletedAt.isNull()
            .or(comment.deletedAt.isNotNull()
                .and(JPAExpressions.selectOne()
                    .from(reply)
                    .where(reply.parentComment.id.eq(comment.id)
                        .and(reply.deletedAt.isNull()))
                    .exists()
                )
            );
    }
}