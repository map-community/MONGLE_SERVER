package com.algangi.mongle.global.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.post.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.post.presentation.dto.ViewUrlResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/files/view-urls")
@RequiredArgsConstructor
public class FileViewUrlIssueController {

    private final ViewUrlIssueService viewUrlIssueService;

    //테스트 api(실제 게시물 파일 조회 url은 게시물 조회 응답으로 포함해야함)
    @PostMapping
    public ApiResponse<ViewUrlResponse> getViewUrls(
        @Valid @RequestBody ViewUrlRequest request) {
        return ApiResponse.success(viewUrlIssueService.issueViewUrls(request));
    }

}
