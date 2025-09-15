package com.algangi.mongle.comment.repository;

import com.algangi.mongle.comment.domain.Comment;
import com.algangi.mongle.comment.domain.CommentSort;
import com.algangi.mongle.comment.domain.QComment;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.algangi.mongle.comment.domain.QComment.comment;
import static com.algangi.mongle.member.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Comment> findParentCommentsWithCursor(Long postId, String cursor, int size, CommentSort sort) {
        QComment reply = new QComment("reply");

        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        comment.parentComment.isNull(),
                        createCursorCondition(cursor, sort),
                        comment.deletedAt.isNull()
                                .or(
                                        JPAExpressions
                                                .selectOne()
                                                .from(reply)
                                                .where(
                                                        reply.parentComment.eq(comment),
                                                        reply.deletedAt.isNull()
                                                )
                                                .exists()
                                )
                )
                .orderBy(createOrderSpecifiers(sort))
                .limit(size + 1)
                .fetch();
    }

    public List<Comment> findRepliesByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }

        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .where(comment.parentComment.id.in(parentIds))
                .orderBy(comment.id.desc())
                .fetch();
    }

    private BooleanExpression createCursorCondition(String cursor, CommentSort sort) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        return switch (sort) {
            case LIKES -> {
                String[] parts = cursor.split("_");
                if (parts.length != 2) {
                    throw invalidCursorException(sort);
                }
                try {
                    long lastLikeCount = Long.parseLong(parts[0]);
                    long lastId = Long.parseLong(parts[1]);
                    yield comment.likeCount.lt(lastLikeCount)
                            .or(comment.likeCount.eq(lastLikeCount).and(comment.id.lt(lastId)));
                } catch (NumberFormatException e) {
                    throw invalidCursorException(sort);
                }
            }
            case LATEST -> {
                try {
                    long lastId = Long.parseLong(cursor);
                    yield comment.id.lt(lastId);
                } catch (NumberFormatException e) {
                    throw invalidCursorException(sort);
                }
            }
        };
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(CommentSort sort) {
        return switch (sort) {
            case LIKES -> new OrderSpecifier[]{
                    comment.likeCount.desc(),
                    comment.id.desc()
            };
            case LATEST -> new OrderSpecifier[]{
                    comment.id.desc()
            };
        };
    }

    private IllegalArgumentException invalidCursorException(CommentSort sort) {
        return new IllegalArgumentException("유효하지 않은 커서 형식입니다 (" + sort.name() + ").");
    }
}