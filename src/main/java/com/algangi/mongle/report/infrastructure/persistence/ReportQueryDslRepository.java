package com.algangi.mongle.report.infrastructure.persistence;

import com.algangi.mongle.report.domain.model.Report;
import com.algangi.mongle.report.domain.repository.ReportQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.algangi.mongle.member.domain.QMember.member;
import static com.algangi.mongle.report.domain.model.QReport.report;

@Repository
@RequiredArgsConstructor
public class ReportQueryDslRepository implements ReportQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Report> findAllBy(Pageable pageable) {
        List<Report> content = queryFactory
            .selectFrom(report)
            .leftJoin(report.reporter, member).fetchJoin()
            .orderBy(report.createdDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(report.count())
            .from(report)
            .fetchOne();
        
        long totalCount = (total == null) ? 0L : total;

        return new PageImpl<>(content, pageable, totalCount);
    }
}