package com.algangi.mongle.comment.domain.repository;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.model.CommentSort;

import java.util.List;
import java.util.Map;

public interface CommentQueryRepository {
    List<Comment> findCommentEntitiesByPost(Long postId, String cursor, int size, CommentSort sort);
    List<Comment> findReplyEntitiesByParent(Long parentId, String cursor, int size, CommentSort sort);
    Map<Long, Boolean> findHasRepliesByParentIds(List<Long> parentIds);
}