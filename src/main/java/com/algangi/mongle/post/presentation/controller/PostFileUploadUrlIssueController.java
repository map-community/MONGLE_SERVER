package com.algangi.mongle.post.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.global.application.service.UploadUrlIssueService;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.post.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.post.presentation.dto.UploadUrlResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/post-files/upload-urls")
@RequiredArgsConstructor
public class PostFileUploadUrlIssueController {

    private final UploadUrlIssueService uploadUrlIssueService;

    @PostMapping
    public ApiResponse<UploadUrlResponse> getUploadUrls(
        @Valid @RequestBody UploadUrlRequest request) {
        return ApiResponse.success(uploadUrlIssueService.issueUploadUrls(request));
    }

}
