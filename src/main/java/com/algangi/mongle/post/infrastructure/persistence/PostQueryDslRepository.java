package com.algangi.mongle.post.infrastructure.persistence;

import com.algangi.mongle.global.util.ParsingUtil;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostSort;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.algangi.mongle.post.domain.model.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostQueryDslRepository implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findPostsByCondition(PostListRequest request) {
        return queryFactory
            .selectFrom(post)
            .where(
                eqPlaceId(request.placeId()),
                eqCloudId(request.cloudId()),
                cursorCondition(request.cursor(), request.sortBy())
            )
            .orderBy(orderSpecifiers(request.sortBy()))
            .limit(request.size() + 1)
            .fetch();
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

    private OrderSpecifier<?>[] orderSpecifiers(@Nullable PostSort postSort) {
        PostSort sort = (postSort == null) ? PostSort.ranking_score : postSort;

        if (sort == PostSort.ranking_score) {
            return new OrderSpecifier[]{
                post.rankingScore.desc(),
                post.createdDate.desc(),
                post.id.desc()
            };
        }

        return new OrderSpecifier[]{
            post.createdDate.desc(),
            post.id.desc()
        };
    }

    private BooleanExpression cursorCondition(@Nullable String cursor,
        @Nullable PostSort postSort) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        PostSort sort = (postSort == null) ? PostSort.ranking_score : postSort;

        String[] parts = cursor.split("_");
        if (sort == PostSort.ranking_score) {
            if (parts.length != 3) {
                return null;
            }
            try {
                Double score = Double.parseDouble(parts[0]);
                LocalDateTime createdAt = ParsingUtil.parseDate(parts[1]);
                String id = parts[2];

                return post.rankingScore.lt(score)
                    .or(post.rankingScore.eq(score)
                        .and(post.createdDate.lt(createdAt))
                    ).or(post.rankingScore.eq(score)
                        .and(post.createdDate.eq(createdAt))
                        .and(post.id.lt(id))
                    );
            } catch (Exception e) {
                return null;
            }

        } else {
            if (parts.length != 2) {
                return null;
            }
            try {
                LocalDateTime createdAt = ParsingUtil.parseDate(parts[0]);
                String id = parts[1];

                return post.createdDate.lt(createdAt)
                    .or(post.createdDate.eq(createdAt)
                        .and(post.id.lt(id))
                    );
            } catch (Exception e) {
                return null;
            }
        }
    }
}

