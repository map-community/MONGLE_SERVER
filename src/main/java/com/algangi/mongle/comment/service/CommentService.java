package com.algangi.mongle.comment.service;

import com.algangi.mongle.comment.domain.Comment;
import com.algangi.mongle.comment.dto.CommentCreateRequest;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.domain.Post;
import com.algangi.mongle.post.service.PostFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    @Transactional
    public void createParentComment(Long postId, CommentCreateRequest dto, Long memberId) {
        Member author = memberFinder.getMemberOrThrow(memberId);
        Post post = postFinder.getPostOrThrow(postId);

        Comment.createParentComment(
                dto.content(),
                post,
                author
        );
    }

    @Transactional
    public void createChildComment(Long parentCommentId, CommentCreateRequest dto, Long memberId) {
        Member author = memberFinder.getMemberOrThrow(memberId);
        Comment comment = commentFinder.getCommentOrThrow(parentCommentId);

        Comment.createChildComment(
                dto.content(),
                comment,
                author
        );
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentFinder.getCommentOrThrow(commentId);
        comment.softDelete();
    }

}