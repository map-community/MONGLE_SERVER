package com.algangi.mongle.comment.infrastructure.persistence;

import com.algangi.mongle.comment.domain.model.QComment;
import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.comment.infrastructure.persistence.querydsl.CommentFilterFactory;
import com.algangi.mongle.comment.infrastructure.persistence.querydsl.CommentOrderFactory;
import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.PaginationResult;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;
import com.querydsl.core.group.GroupBy;
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
    private final CommentFilterFactory filterFactory;
    private final CommentOrderFactory orderFactory;

    @Override
    public PaginationResult<Comment> findCommentsByPost(CommentSearchCondition condition,
        int size) {
        List<Comment> comments = queryFactory
            .selectFrom(comment)
            .leftJoin(comment.member).fetchJoin()
            .where(
                filterFactory.eqPostId(condition.postId()),
                filterFactory.isParentComment(),
                filterFactory.cursorCondition(condition.cursor(), condition.sort()),
                filterFactory.visibleCommentCondition()
            )
            .orderBy(orderFactory.createOrderSpecifiers(condition.sort()))
            .limit(size + 1)
            .fetch();

        return PaginationResult.of(comments, size);
    }

    @Override
    public PaginationResult<Comment> findRepliesByParent(ReplySearchCondition condition, int size) {
        List<Comment> replies = queryFactory
            .selectFrom(comment)
            .leftJoin(comment.member).fetchJoin()
            .where(
                filterFactory.eqParentId(condition.parentId()),
                comment.deletedAt.isNull(),
                filterFactory.cursorCondition(condition.cursor(), condition.sort())
            )
            .orderBy(orderFactory.createOrderSpecifiers(condition.sort()))
            .limit(size + 1)
            .fetch();

        return PaginationResult.of(replies, size);
    }

    @Override
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

    @Override
    public long countByPostId(String postId) {
        if (postId == null) {
            return 0L;
        }
        Long count = queryFactory
            .select(comment.count())
            .from(comment)
            .where(comment.post.id.eq(postId), comment.deletedAt.isNull())
            .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Map<String, Long> countCommentsByPostIds(List<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return queryFactory
            .from(comment)
            .where(comment.post.id.in(postIds), comment.deletedAt.isNull())
            .groupBy(comment.post.id)
            .transform(GroupBy.groupBy(comment.post.id).as(comment.count()));
    }
}
