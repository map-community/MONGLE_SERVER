package com.algangi.mongle.comment.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algangi.mongle.comment.application.service.CommentCommandService;
import com.algangi.mongle.comment.application.service.CommentQueryService;
import com.algangi.mongle.comment.presentation.cursor.CursorInfoResponse;
import com.algangi.mongle.comment.presentation.dto.CommentCreateRequest;
import com.algangi.mongle.comment.presentation.dto.CommentInfoResponse;
import com.algangi.mongle.comment.presentation.dto.CommentQueryRequest;
import com.algangi.mongle.comment.presentation.mapper.CommentRequestMapper;
import com.algangi.mongle.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;
    private final CommentRequestMapper commentRequestMapper;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CursorInfoResponse<CommentInfoResponse>>> getCommentsByPost(
            @PathVariable(name = "postId") String postId,
            @ModelAttribute CommentQueryRequest request
    ) {
        var condition = commentRequestMapper.toPostCommentSearchCondition(postId, request);
        var result = commentQueryService.getCommentsByPost(condition, request.memberId(), request.size());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/comments/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<CursorInfoResponse<CommentInfoResponse>>> getRepliesByParent(
            @PathVariable(name = "parentCommentId") String parentCommentId,
            @ModelAttribute CommentQueryRequest request
    ) {
        var condition = commentRequestMapper.toReplySearchCondition(parentCommentId, request);
        var result = commentQueryService.getRepliesByParent(condition, request.memberId(), request.size());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Void>> createParentComment(
        @PathVariable(name = "postId") String postId,
        @Valid @RequestBody CommentCreateRequest dto,
        @RequestParam String memberId)  {
        commentCommandService.createParentComment(postId, dto.content(), memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/comments/{parentCommentId}/replies")
    public ResponseEntity<ApiResponse<Void>> createChildComment(
            @PathVariable(name = "parentCommentId") String parentCommentId,
            @Valid @RequestBody CommentCreateRequest dto,
            @RequestParam String memberId) {
        commentCommandService.createChildComment(parentCommentId, dto.content(), memberId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable(name = "commentId") String commentId) {
        commentCommandService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}