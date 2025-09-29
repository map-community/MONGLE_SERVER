package com.algangi.mongle.comment.domain.repository;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.PaginationResult;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;

import java.util.List;
import java.util.Map;

public interface CommentQueryRepository {

    PaginationResult<Comment> findCommentsByPost(CommentSearchCondition condition, int size);

    PaginationResult<Comment> findRepliesByParent(ReplySearchCondition condition, int size);

    Map<String, Boolean> findHasRepliesByParentIds(List<String> parentIds);

    long countByPostId(String postId);

    Map<String, Long> countCommentsByPostIds(List<String> postIds);
}