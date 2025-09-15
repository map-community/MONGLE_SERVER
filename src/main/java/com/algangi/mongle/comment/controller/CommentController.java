package com.algangi.mongle.comment.controller;

import com.algangi.mongle.comment.dto.CommentCreateRequest;
import com.algangi.mongle.comment.service.CommentService;
import com.algangi.mongle.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Void>> createParentComment(@PathVariable(name = "postId") Long postId,
                                                                 @Valid @RequestBody CommentCreateRequest dto,
                                                                 @RequestParam Long memberId) {
        commentService.createParentComment(postId, dto, memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}