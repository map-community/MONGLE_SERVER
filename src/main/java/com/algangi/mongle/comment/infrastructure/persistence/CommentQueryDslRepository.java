package com.algangi.mongle.comment.infrastructure.persistence;

import com.algangi.mongle.comment.domain.model.QComment;
import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.comment.infrastructure.persistence.querydsl.CommentFilterFactory;
import com.algangi.mongle.comment.infrastructure.persistence.querydsl.CommentOrderFactory;
import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;
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
    public List<Comment> findCommentsByPost(CommentSearchCondition condition, int size) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member).fetchJoin()
                .where(
                        filterFactory.eqPostId(condition.postId()),
                        filterFactory.isParentComment(),
                        filterFactory.cursorCondition(condition.cursor(), condition.sort()),
                        filterFactory.visibleCommentCondition()
                )
                .orderBy(orderFactory.createOrderSpecifiers(condition.sort()))
                .limit(size)
                .fetch();
    }

    @Override
    public List<Comment> findRepliesByParent(ReplySearchCondition condition, int size) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member).fetchJoin()
                .where(
                        filterFactory.eqParentId(condition.parentId()),
                        filterFactory.cursorCondition(condition.cursor(), condition.sort())
                )
                .orderBy(orderFactory.createOrderSpecifiers(condition.sort()))
                .limit(size)
                .fetch();
    }

    public Map<Long, Boolean> findHasRepliesByParentIds(List<Long> parentIds) {
        // 1. 부모 리스트가 비어있으면 빈 맵 반환
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. 대댓글이 존재하는 부모 ID 리스트 조회
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

        // 3. List -> Set(빠른 연산을 위해) -> Map으로 변환
        Set<Long> parentIdsWithRepliesSet = new HashSet<>(parentIdsWithReplies);
        return parentIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        parentIdsWithRepliesSet::contains
                ));
    }

}