package com.algangi.mongle.reaction.infrastructure.persistence;

import com.algangi.mongle.comment.domain.model.QComment;
import com.algangi.mongle.post.domain.model.QPost;
import com.algangi.mongle.reaction.domain.model.QReaction;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.stats.application.dto.ReactionCleanupDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class ReactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public List<ReactionCleanupDto> findAllReactionCleanupData(String memberId) {
        QReaction r = QReaction.reaction;
        QComment c = QComment.comment;
        QPost p = QPost.post;

        // 1. POST 리액션 조회
        List<ReactionCleanupDto> postData = queryFactory
                .select(Projections.constructor(ReactionCleanupDto.class,
                        r.targetId,
                        r.targetType,
                        r.type,
                        r.targetId
                ))
                .from(r)
                .where(r.member.memberId.eq(memberId).and(r.targetType.eq(TargetType.POST)))
                .fetch();

        // 2. COMMENT 리액션 조회
        List<ReactionCleanupDto> commentData = queryFactory
                .select(Projections.constructor(ReactionCleanupDto.class,
                        r.targetId,
                        r.targetType,
                        r.type,
                        p.id
                ))
                .from(r)
                .join(c).on(r.targetId.eq(c.id))
                .join(c.post, p)
                .where(
                        r.member.memberId.eq(memberId)
                                .and(r.targetType.eq(TargetType.COMMENT))
                )
                .fetch();

        // 3. 두 결과 합침
        return Stream.concat(postData.stream(), commentData.stream()).toList();
    }
}