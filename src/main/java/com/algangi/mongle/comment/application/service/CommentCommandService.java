package com.algangi.mongle.comment.application.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.comment.presentation.dto.CommentCreateRequest;
import com.algangi.mongle.comment.domain.service.CommentDomainService;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.domain.Post;
import com.algangi.mongle.post.service.PostFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;
    private final CommentDomainService commentDomainService;
    private final CommentRepository commentRepository;

    @Transactional
    public void createParentComment(Long postId, String content, Long memberId) {
        Member author = memberFinder.getMemberOrThrow(memberId);
        Post post = postFinder.getPostOrThrow(postId);

        Comment newComment = commentDomainService.createParentComment(post, author, content);

        commentRepository.save(newComment);
    }

    @Transactional
    public void createChildComment(Long parentCommentId, String content, Long memberId) {
        Member author = memberFinder.getMemberOrThrow(memberId);
        Comment parent = commentFinder.getCommentOrThrow(parentCommentId);

        Comment newComment = commentDomainService.createChildComment(parent, author, content);

        commentRepository.save(newComment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentFinder.getCommentOrThrow(commentId);

        commentDomainService.deleteComment(comment);
    }

}