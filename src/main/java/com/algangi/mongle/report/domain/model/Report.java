package com.algangi.mongle.report.domain.model;

import com.algangi.mongle.global.entity.CreatedDateBaseEntity;
import com.algangi.mongle.member.domain.Member;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "report", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_report_reporter_target",
        columnNames = {"reporter_id", "target_id", "target_type"}
    )
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends CreatedDateBaseEntity {

    @Id
    @Tsid
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportedTargetType targetType;

    @Column(nullable = false)
    private String targetAuthorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    @Builder.Default
    private ReportStatus reportStatus = ReportStatus.RECEIVED;

    public void updateStatus(ReportStatus newStatus) {
        this.reportStatus = newStatus;
    }
}