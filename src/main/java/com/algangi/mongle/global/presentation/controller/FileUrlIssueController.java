package com.algangi.mongle.global.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.global.application.service.UploadUrlIssueService;
import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.global.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.global.presentation.dto.UploadUrlResponse;
import com.algangi.mongle.global.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.global.presentation.dto.ViewUrlResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileUrlIssueController {

    private final UploadUrlIssueService uploadUrlIssueService;
    private final ViewUrlIssueService viewUrlIssueService;

    @PostMapping("/upload-urls")
    public ApiResponse<UploadUrlResponse> getUploadUrls(
        @Valid @RequestBody UploadUrlRequest request) {
        return ApiResponse.success(uploadUrlIssueService.issueUploadUrls(request));
    }

    @PostMapping("/view-urls")
    public ApiResponse<ViewUrlResponse> getViewUrls(
        @Valid @RequestBody ViewUrlRequest request) {
        return ApiResponse.success(viewUrlIssueService.issueViewUrls(request));
    }

}
