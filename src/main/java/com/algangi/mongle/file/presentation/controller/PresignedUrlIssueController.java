package com.algangi.mongle.file.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.file.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.file.presentation.dto.UploadUrlResponse;
import com.algangi.mongle.file.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.file.presentation.dto.ViewUrlResponse;
import com.algangi.mongle.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/files/")
@RequiredArgsConstructor
public class PresignedUrlIssueController {

    private final FileService fileService;

    @PostMapping("upload-urls")
    public ApiResponse<UploadUrlResponse> getUploadUrls(
        @Valid @RequestBody UploadUrlRequest request) {
        return ApiResponse.success(fileService.issueUploadUrls(request));
    }

    //테스트 api(실제 게시물 파일 조회 url은 게시물 조회 응답으로 포함해야함)
    @PostMapping("/view-urls")
    public ApiResponse<ViewUrlResponse> getViewUrls(
        @Valid @RequestBody ViewUrlRequest request) {
        return ApiResponse.success(fileService.issueViewUrls(request));
    }
}
