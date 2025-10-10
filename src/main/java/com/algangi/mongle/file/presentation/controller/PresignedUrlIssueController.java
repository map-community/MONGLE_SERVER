package com.algangi.mongle.file.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.file.domain.FileType;
import com.algangi.mongle.file.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.file.presentation.dto.UploadUrlResponse;
import com.algangi.mongle.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/upload-urls/")
@RequiredArgsConstructor
public class PresigneUrlIssueController {

    private final FileService fileService;

    @PostMapping
    public ApiResponse<UploadUrlResponse> getUploadUrls(
        @Valid @RequestBody UploadUrlRequest request, FileType fileType) {
        return ApiResponse.success(fileService.issueUploadUrls(fileType, request));
    }

}
