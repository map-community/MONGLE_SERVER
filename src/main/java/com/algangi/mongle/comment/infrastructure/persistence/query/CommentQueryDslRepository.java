package com.algangi.mongle.comment.infrastructure.persistence.query;

import com.algangi.mongle.comment.domain.model.QComment;
import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.model.CommentSort;
import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Collections;

import static com.algangi.mongle.comment.domain.model.QComment.comment;

@Repository
@RequiredArgsConstructor
public class CommentQueryDslRepository implements CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Comment> findCommentEntitiesByPost(
            Long postId, String cursor, int size, CommentSort sort) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        comment.parentComment.isNull(),
                        CommentCursorParser.parse(cursor, sort),
                        visibleCommentCondition()
                )
                .orderBy(CommentOrderSpecifiers.of(sort))
                .limit(size)
                .fetch();
    }

    public List<Comment> findReplyEntitiesByParent(
            Long parentId, String cursor, int size, CommentSort sort) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member).fetchJoin()
                .where(
                        comment.parentComment.id.eq(parentId),
                        CommentCursorParser.parse(cursor, sort)
                )
                .orderBy(CommentOrderSpecifiers.of(sort))
                .limit(size)
                .fetch();
    }

    public Map<Long, Boolean> findHasRepliesByParentIds(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QComment reply = new QComment("reply");
        List<Long> parentIdsWithReplies = queryFactory
                .select(reply.parentComment.id)
                .from(reply)
                .where(
                        reply.parentComment.id.in(parentIds),
                        reply.deletedAt.isNull()
                )
                .groupBy(reply.parentComment.id)
                .fetch();

        Set<Long> parentIdsWithRepliesSet = new HashSet<>(parentIdsWithReplies);
        return parentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        parentIdsWithRepliesSet::contains
                ));
    }

    private BooleanExpression visibleCommentCondition() {
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