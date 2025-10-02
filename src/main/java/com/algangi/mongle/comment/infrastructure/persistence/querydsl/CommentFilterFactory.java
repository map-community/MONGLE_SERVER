package com.algangi.mongle.comment.infrastructure.persistence.querydsl;

import static com.algangi.mongle.comment.domain.model.QComment.comment;

import org.springframework.stereotype.Component;

import com.algangi.mongle.comment.domain.model.CommentSort;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.List;

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

    public BooleanExpression notInBlockedMemberIds(List<String> blockedMemberIds) {
        if (blockedMemberIds == null || blockedMemberIds.isEmpty()) {
            return null;
        }
        return comment.member.memberId.notIn(blockedMemberIds);
    }
}