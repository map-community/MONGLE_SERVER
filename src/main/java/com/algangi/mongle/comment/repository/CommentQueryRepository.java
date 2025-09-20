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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Collections;

import static com.algangi.mongle.comment.domain.QComment.comment;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Comment> findCommentEntitiesByPost(
            Long postId, String cursor, int size, CommentSort sort) {

        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        comment.parentComment.isNull(),
                        cursorCondition(cursor, sort),
                        visibleCommentCondition()
                )
                .orderBy(getOrderSpecifiers(sort))
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
                        cursorCondition(cursor, sort)
                )
                .orderBy(getOrderSpecifiers(sort))
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

    private BooleanExpression cursorCondition(String cursor, CommentSort sort) {
        if (cursor == null || cursor.isBlank()) return null;

        try {
            String[] parts = cursor.split("_");
            return switch (sort) {
                case LATEST -> latestCondition(parts);
                case LIKES -> likesCondition(parts);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 커서 값입니다.", e);
        }
    }

    private BooleanExpression latestCondition(String[] parts) {
        if (parts.length != 2) throw new IllegalArgumentException("잘못된 커서 형식입니다.");
        LocalDateTime created = parseDate(parts[0]);
        Long id = parseLong(parts[1]);
        return comment.createdDate.lt(created)
                .or(comment.createdDate.eq(created).and(comment.id.lt(id)));
    }

    private BooleanExpression likesCondition(String[] parts) {
        if (parts.length != 3) throw new IllegalArgumentException("잘못된 커서 형식입니다.");
        Long like = parseLong(parts[0]);
        LocalDateTime created = parseDate(parts[1]);
        Long id = parseLong(parts[2]);

        return comment.likeCount.lt(like)
                .or(comment.likeCount.eq(like).and(comment.createdDate.lt(created)))
                .or(comment.likeCount.eq(like).and(comment.createdDate.eq(created)).and(comment.id.lt(id)));
    }

    private LocalDateTime parseDate(String s) {
        try { return LocalDateTime.parse(s); }
        catch (Exception e) { throw new IllegalArgumentException("잘못된 날짜 형식입니다.", e); }
    }

    private Long parseLong(String s) {
        try { return Long.parseLong(s); }
        catch (Exception e) { throw new IllegalArgumentException("잘못된 숫자 형식입니다.", e); }
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

    private OrderSpecifier<?>[] getOrderSpecifiers(CommentSort sort) {
        if (sort == null) {
            sort = CommentSort.LATEST;
        }
        return sort.getOrderSpecifiers();
    }
}