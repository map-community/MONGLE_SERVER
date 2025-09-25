package com.algangi.mongle.global.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.global.application.service.UploadUrlIssueService;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.global.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.global.presentation.dto.UploadUrlResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload-urls")
public class FileUrlIssueController {

    private final UploadUrlIssueService uploadUrlIssueService;

    @PostMapping
    public ApiResponse<UploadUrlResponse> getUploadUrls(
        @Valid @RequestBody UploadUrlRequest request) {
        return ApiResponse.success(uploadUrlIssueService.issueUploadUrls(request));
    }

}
