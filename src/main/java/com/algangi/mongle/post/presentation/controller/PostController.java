package com.algangi.mongle.post.presentation.controller;

import com.algangi.mongle.post.presentation.dto.PostDetailResponse;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.post.application.service.PostCreationService;
import com.algangi.mongle.post.application.service.PostQueryService;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;
import com.algangi.mongle.post.presentation.dto.PostResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostCreationService postCreationService;
    private final PostQueryService postQueryService;

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
        @Valid @RequestBody PostCreateRequest dto) {
        return ResponseEntity.ok(ApiResponse.success(postCreationService.createPost(dto)));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PostListResponse>> getPostList(
        @ModelAttribute @Valid PostListRequest request) {
        PostListResponse response = postQueryService.getPostList(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
        @PathVariable String postId) {
        PostDetailResponse response = postQueryService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

