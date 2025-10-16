package com.algangi.mongle.comment.domain.model;

import com.algangi.mongle.comment.exception.CommentErrorCode;
import com.algangi.mongle.global.entity.TimeBaseEntity;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.model.Member;
import com.algangi.mongle.post.domain.model.Post;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Comment extends TimeBaseEntity implements CursorConvertible {

    @Id
    @Tsid
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private long likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private long dislikeCount = 0;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommentStatus status = CommentStatus.ACTIVE;

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

    public boolean isDeleted() {
        return this.status == CommentStatus.DELETED_BY_USER
            || this.status == CommentStatus.DELETED_BY_ADMIN
            || this.status == CommentStatus.DELETED_BY_WITHDRAWAL;
    }

    public void softDeleteByUser() {
        if (this.status != CommentStatus.ACTIVE) {
            throw new ApplicationException(CommentErrorCode.ALREADY_DELETED);
        }
        this.status = CommentStatus.DELETED_BY_USER;
    }

    public void softDeleteByAdmin() {
        if (this.status != CommentStatus.ACTIVE) {
            throw new ApplicationException(CommentErrorCode.ALREADY_DELETED);
        }
        this.status = CommentStatus.DELETED_BY_ADMIN;
    }

    public void increaseLikeCount(long delta) {
        if (delta == 0) {
            return;
        }
        long newCount = this.likeCount + delta;
        this.likeCount = Math.max(newCount, 0);
    }

    public void increaseDislikeCount(long delta) {
        if (delta == 0) {
            return;
        }
        long newCount = this.dislikeCount + delta;
        this.dislikeCount = Math.max(newCount, 0);
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public long getLikeCount() {
        return this.likeCount;
    }

    @Override
    public Instant getCreatedAt() {
        return this.getCreatedDate();
    }

}