package com.algangi.mongle.report.presentation.controller;

import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.report.application.service.ReportCommandService;
import com.algangi.mongle.report.presentation.dto.ReportCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportCommandService reportCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createReport(
        @RequestParam String memberId, // TODO: 인증 기능 구현 후 @AuthenticationPrincipal로 변경
        @Valid @RequestBody ReportCreateRequest request) {
        reportCommandService.createReport(memberId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}