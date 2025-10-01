package com.algangi.mongle.report.domain.repository;

import com.algangi.mongle.report.domain.model.Report;
import com.algangi.mongle.report.domain.model.ReportedTargetType;
import com.algangi.mongle.report.domain.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, String> {

    boolean existsByReporter_MemberIdAndTargetIdAndTargetType(String reporterId, String targetId,
        ReportedTargetType targetType);

    long countByTargetAuthorIdAndStatus(String targetAuthorId, ReportStatus status);
}