package com.algangi.mongle.comment.presentation.mapper;

import org.springframework.stereotype.Component;

import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;
import com.algangi.mongle.comment.presentation.dto.CommentQueryRequest;

@Component
public class CommentRequestMapper {

    public CommentSearchCondition toPostCommentSearchCondition(String postId,
        CommentQueryRequest request) {
        return new CommentSearchCondition(postId, request.cursor(), request.sort());
    }

    public ReplySearchCondition toReplySearchCondition(Long parentId, CommentQueryRequest request) {
        return new ReplySearchCondition(parentId, request.cursor(), request.sort());
    }

}