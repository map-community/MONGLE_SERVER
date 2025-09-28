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

    // 아래 메서드는 Commit 3에서 사용될 예정입니다.
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
        @PathVariable String postId) {
        // TODO: Commit 3에서 상세 조회 로직 구현 예정. 현재는 임시 응답 반환.
        // PostDetailResponse response = postQueryService.getPostDetail(postId);
        // return ResponseEntity.ok(ApiResponse.success("게시글 상세 조회에 성공했습니다.", response));
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

