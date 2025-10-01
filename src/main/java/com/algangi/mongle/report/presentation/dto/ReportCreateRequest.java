package com.algangi.mongle.report.presentation.dto;

import com.algangi.mongle.report.domain.model.ReportReason;
import com.algangi.mongle.report.domain.model.ReportedTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
    @NotBlank(message = "신고 대상 ID는 필수입니다.")
    String targetId,

    @NotNull(message = "신고 대상 타입은 필수입니다.")
    ReportedTargetType targetType,

    @NotNull(message = "신고 사유는 필수입니다.")
    ReportReason reason
) {

}