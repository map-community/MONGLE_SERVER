package com.algangi.mongle.comment.domain.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.post.domain.Post;
import org.springframework.stereotype.Service;

@Service
public class CommentDomainService {

    public Comment createParentComment(Post post, Member author, String content) {
        return Comment.createParentComment(content, post, author);
    }

    public Comment createChildComment(Comment parent, Member author, String content) {
        return Comment.createChildComment(content, parent, author);
    }

    public void deleteComment(Comment comment) {
        comment.softDelete();
    }

}