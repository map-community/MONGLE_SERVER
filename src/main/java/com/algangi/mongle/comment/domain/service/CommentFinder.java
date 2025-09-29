package com.algangi.mongle.comment.domain.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.exception.CommentErrorCode;
import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class CommentFinder {

    private final CommentRepository commentJpaRepository;

    public Comment getCommentOrThrow(String commentId) {
        return commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ApplicationException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

}
