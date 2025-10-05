package com.algangi.mongle.report.presentation.controller;

import com.algangi.mongle.auth.infrastructure.security.authentication.CustomUserDetails;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.report.application.service.ReportCommandService;
import com.algangi.mongle.report.presentation.dto.ReportCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportCommandService reportCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createReport(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ReportCreateRequest request) {
        reportCommandService.createReport(userDetails.userId(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}