package com.algangi.mongle.post.infrastructure.persistence;

import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryDslRepository implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    // TODO: 조회 메서드 구현 예정
}
