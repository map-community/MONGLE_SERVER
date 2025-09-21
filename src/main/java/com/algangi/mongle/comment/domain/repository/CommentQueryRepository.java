package com.algangi.mongle.comment.domain.repository;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.infrastructure.persistence.vo.CommentSearchCondition;
import com.algangi.mongle.comment.infrastructure.persistence.vo.ReplySearchCondition;

import java.util.List;
import java.util.Map;

public interface CommentQueryRepository {
    List<Comment> findCommentsByPost(CommentSearchCondition condition, int size);
    List<Comment> findRepliesByParent(ReplySearchCondition condition, int size);
    Map<Long, Boolean> findHasRepliesByParentIds(List<Long> parentIds);
}