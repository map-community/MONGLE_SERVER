package com.algangi.mongle.report.presentation.dto;

import com.algangi.mongle.report.domain.model.Report;
import com.algangi.mongle.report.domain.model.ReportReason;
import com.algangi.mongle.report.domain.model.ReportStatus;
import com.algangi.mongle.report.domain.model.ReportedTargetType;

import java.time.LocalDateTime;

public record ReportAdminResponse(
    String reportId,
    ReporterInfo reporter,
    String targetId,
    ReportedTargetType targetType,
    TargetAuthorInfo targetAuthor,
    ReportReason reason,
    ReportStatus reportStatus,
    LocalDateTime createdAt
) {

    public static ReportAdminResponse from(Report report) {
        return new ReportAdminResponse(
            report.getId(),
            ReporterInfo.from(report.getReporter()),
            report.getTargetId(),
            report.getTargetType(),
            new TargetAuthorInfo(report.getTargetAuthorId()),
            report.getReason(),
            report.getReportStatus(),
            report.getCreatedDate()
        );
    }

    public record ReporterInfo(String memberId, String nickname) {

        public static ReporterInfo from(com.algangi.mongle.member.domain.Member member) {
            return new ReporterInfo(member.getMemberId(), member.getNickname());
        }
    }

    public record TargetAuthorInfo(String memberId) {

    }
}