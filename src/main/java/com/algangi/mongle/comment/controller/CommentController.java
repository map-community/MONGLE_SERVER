package com.algangi.mongle.comment.controller;

import com.algangi.mongle.comment.dto.CommentCreateRequest;
import com.algangi.mongle.comment.service.CommentService;
import com.algangi.mongle.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping("/comments/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<Void>> createChildComment(@PathVariable(name = "parentCommentId") Long parentCommentId,
                                                                @Valid @RequestBody CommentCreateRequest dto,
                                                                @RequestParam Long memberId) {
        commentService.createChildComment(parentCommentId, dto, memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable(name = "commentId") Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}