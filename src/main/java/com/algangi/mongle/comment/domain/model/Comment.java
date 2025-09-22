package com.algangi.mongle.comment.domain.model;

import com.algangi.mongle.comment.exception.CommentErrorCode;
import com.algangi.mongle.global.entity.TimeBaseEntity;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.post.domain.model.Post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Comment extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private long likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private long dislikeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static Comment createParentComment(String content, Post post, Member member) {
        Comment comment = Comment.builder()
            .content(content)
            .post(post)
            .parentComment(null)
            .member(member)
            .build();

        post.addComment(comment);

        return comment;
    }

    public static Comment createChildComment(String content, Comment parentComment, Member member) {
        if (parentComment.isChildComment()) {
            throw new IllegalArgumentException("대댓글에 대댓글을 달 수 없습니다.");
        }

        Comment comment = Comment.builder()
            .content(content)
            .parentComment(parentComment)
            .post(parentComment.getPost())
            .member(member)
            .build();

        parentComment.getPost().addComment(comment);

        return comment;
    }

    public boolean isChildComment() {
        return parentComment != null;
    }

    public boolean isDeleted() { return deletedAt != null; }

    public void softDelete() {
        if (isDeleted()) {
            throw new ApplicationException(CommentErrorCode.ALREADY_DELETED);
        }
        this.deletedAt = LocalDateTime.now();
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
