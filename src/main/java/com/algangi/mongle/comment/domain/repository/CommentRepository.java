package com.algangi.mongle.comment.domain.repository;

import com.algangi.mongle.comment.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
