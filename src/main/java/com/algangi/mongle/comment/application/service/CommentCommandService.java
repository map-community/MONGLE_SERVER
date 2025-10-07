package com.algangi.mongle.comment.application.service;

import com.algangi.mongle.comment.application.event.CommentCreatedEvent;
import com.algangi.mongle.comment.application.event.CommentDeletedEvent;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.MemberStatus;
import com.algangi.mongle.member.exception.MemberErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.comment.domain.service.CommentDomainService;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;
    private final CommentDomainService commentDomainService;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createParentComment(String postId, String content, String memberId) {
        Member author = memberFinder.getMemberOrThrow(memberId);
        if (author.getStatus() == MemberStatus.BANNED) {
            throw new ApplicationException(MemberErrorCode.MEMBER_IS_BANNED);
        }
        Post post = postFinder.getPostOrThrow(postId);

        Comment newComment = commentDomainService.createParentComment(post, author, content);

        commentRepository.save(newComment);
        eventPublisher.publishEvent(new CommentCreatedEvent(postId, newComment.getId()));
    }

    @Transactional
    public void createChildComment(String parentCommentId, String content, String memberId) {
        Member author = memberFinder.getMemberOrThrow(memberId);
        if (author.getStatus() == MemberStatus.BANNED) {
            throw new ApplicationException(MemberErrorCode.MEMBER_IS_BANNED);
        }
        Comment parent = commentFinder.getCommentOrThrow(parentCommentId);

        Comment newComment = commentDomainService.createChildComment(parent, author, content);

        commentRepository.save(newComment);
        eventPublisher.publishEvent(
            new CommentCreatedEvent(parent.getPost().getId(), newComment.getId()));
    }

    @Transactional
    public void deleteComment(String commentId, String memberId) {
        Comment comment = commentFinder.getCommentOrThrow(commentId);
        boolean wasAlreadyDeleted = comment.isDeleted();
        commentDomainService.deleteComment(comment);

        if (!wasAlreadyDeleted) {
            eventPublisher.publishEvent(new CommentDeletedEvent(comment.getPost().getId()));
        }
    }
}