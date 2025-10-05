package com.algangi.mongle.report.presentation.controller;

import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.report.application.service.ReportCommandService;
import com.algangi.mongle.report.application.service.ReportQueryService;
import com.algangi.mongle.report.domain.model.ReportStatus;
import com.algangi.mongle.report.presentation.dto.ReportAdminResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportQueryService reportQueryService;
    private final ReportCommandService reportCommandService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReportAdminResponse>>> getReportList(
        @PageableDefault(size = 20) Pageable pageable) {
        Page<ReportAdminResponse> reportList = reportQueryService.getReportList(pageable);
        return ResponseEntity.ok(ApiResponse.success(reportList));
    }

    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<Void>> updateReportStatus(
        @PathVariable String reportId,
        @RequestParam ReportStatus status) {
        reportCommandService.updateReportStatus(reportId, status);
        return ResponseEntity.ok(ApiResponse.success());
    }
}