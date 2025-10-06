package com.algangi.mongle.post.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.auth.infrastructure.security.authentication.CustomUserDetails;
import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.post.application.service.PostCommandService;
import com.algangi.mongle.post.application.service.PostCreationService;
import com.algangi.mongle.post.application.service.PostQueryService;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;
import com.algangi.mongle.post.presentation.dto.PostDetailResponse;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import com.algangi.mongle.post.presentation.dto.PostResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostCreationService postCreationService;
    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;

    // 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
        @Valid @RequestBody PostCreateRequest dto,
        @AuthenticationPrincipal CustomUserDetails user) {
        PostResponse response = postCreationService.createPost(dto, user.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 게시글 목록 조회 (정적/동적 구름 내부)
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PostListResponse>> getPostList(
        @Valid @ModelAttribute PostListRequest request,
        @AuthenticationPrincipal CustomUserDetails user) {
        String memberId = (user != null) ? user.userId() : null;
        PostListResponse response = postQueryService.getPostList(request, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails user) {
        String memberId = (user != null) ? user.userId() : null;
        PostDetailResponse response = postQueryService.getPostDetail(postId, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
        @PathVariable String postId,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        postCommandService.deletePost(postId, user.userId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}

