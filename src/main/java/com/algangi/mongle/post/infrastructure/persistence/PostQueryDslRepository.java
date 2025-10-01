package com.algangi.mongle.post.infrastructure.persistence;

import com.algangi.mongle.global.util.ParsingUtil;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostSort;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.algangi.mongle.post.domain.model.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostQueryDslRepository implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findPostsByCondition(PostListRequest request, List<String> blockedAuthorIds) {
        JPAQuery<Post> query = queryFactory
            .selectFrom(post)
            .where(
                eqPlaceId(request.placeId()),
                eqCloudId(request.cloudId()),
                cursorCondition(request.cursor(), request.sortBy()),
                notInBlockedAuthorIds(blockedAuthorIds)
            )
            .limit(request.size() + 1);

        applySorting(query, request.sortBy());
        return query.fetch();
    }

    @Override
    public List<Post> findGrainsInCells(List<String> s2cellTokens, List<String> blockedAuthorIds) {
        return queryFactory
            .selectFrom(post)
            .where(
                post.s2TokenId.in(s2cellTokens),
                post.staticCloudId.isNull(),
                post.dynamicCloudId.isNull(),
                notInBlockedAuthorIds(blockedAuthorIds)
            )
            .fetch();
    }

    @Override
    public Map<Long, Long> countPostsByStaticCloudIds(List<Long> cloudIds) {
        return queryFactory
            .from(post)
            .where(post.staticCloudId.in(cloudIds))
            .groupBy(post.staticCloudId)
            .transform(GroupBy.groupBy(post.staticCloudId).as(post.count()));
    }

    @Override
    public Map<Long, Long> countPostsByDynamicCloudIds(List<Long> cloudIds) {
        return queryFactory
            .from(post)
            .where(post.dynamicCloudId.in(cloudIds))
            .groupBy(post.dynamicCloudId)
            .transform(GroupBy.groupBy(post.dynamicCloudId).as(post.count()));
    }

    private BooleanExpression notInBlockedAuthorIds(List<String> blockedAuthorIds) {
        if (blockedAuthorIds == null || blockedAuthorIds.isEmpty()) {
            return null;
        }
        return post.authorId.notIn(blockedAuthorIds);
    }

    private void applySorting(JPAQuery<Post> query, @Nullable PostSort sortBy) {
        PostSort sort = (sortBy == null) ? PostSort.ranking_score : sortBy;

        switch (sort) {
            case ranking_score ->
                query.orderBy(post.rankingScore.desc(), post.createdDate.desc(), post.id.desc());
            case createdAt -> query.orderBy(post.createdDate.desc(), post.id.desc());
        }
    }

    private BooleanExpression eqPlaceId(String placeId) {
        if (!StringUtils.hasText(placeId)) {
            return null;
        }
        try {
            return post.staticCloudId.eq(Long.parseLong(placeId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BooleanExpression eqCloudId(String cloudId) {
        if (!StringUtils.hasText(cloudId)) {
            return null;
        }
        try {
            return post.dynamicCloudId.eq(Long.parseLong(cloudId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BooleanExpression cursorCondition(String cursor, PostSort sort) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        PostSort finalSort = (sort == null) ? PostSort.ranking_score : sort;

        String[] parts = cursor.split("_");
        if (finalSort == PostSort.ranking_score) {
            if (parts.length != 3) {
                return null;
            }
            Double score = ParsingUtil.parse(parts[0], Double::parseDouble, "잘못된 커서 형식");
            LocalDateTime createdAt = ParsingUtil.parseDate(parts[1]);
            String id = parts[2];

            return post.rankingScore.lt(score)
                .or(post.rankingScore.eq(score)
                    .and(post.createdDate.lt(createdAt))
                ).or(post.rankingScore.eq(score)
                    .and(post.createdDate.eq(createdAt))
                    .and(post.id.lt(id))
                );

        } else {
            if (parts.length != 2) {
                return null;
            }
            LocalDateTime createdAt = ParsingUtil.parseDate(parts[0]);
            String id = parts[1];

            return post.createdDate.lt(createdAt)
                .or(post.createdDate.eq(createdAt)
                    .and(post.id.lt(id))
                );
        }
    }
}