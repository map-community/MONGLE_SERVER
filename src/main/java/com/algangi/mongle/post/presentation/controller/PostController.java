package com.algangi.mongle.post.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.post.application.dto.PostCreationCommand;
import com.algangi.mongle.post.application.service.PostCreationService;
import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;
import com.algangi.mongle.post.presentation.dto.PostResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostCreationService postApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
        @Valid @RequestBody PostCreateRequest dto) {
        PostCreationCommand command = new PostCreationCommand(
            Location.create(dto.latitude(), dto.longitude()),
            dto.s2TokenId(),
            dto.title(),
            dto.content(),
            dto.authorId());

        return ResponseEntity.ok(ApiResponse.success(postApplicationService.createPost(command)));
    }
}
