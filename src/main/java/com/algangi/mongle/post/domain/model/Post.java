package com.algangi.mongle.post.domain.model;

import java.util.ArrayList;
import java.util.List;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.global.annotation.ULID;
import com.algangi.mongle.global.entity.TimeBaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Post extends TimeBaseEntity {

    @Id
    @ULID
    private String id;

    @Embedded
    private Location location;

    @Column(nullable = false)
    private String s2TokenId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private long viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private long commentCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private long likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private long dislikeCount = 0;

    @Builder.Default
    private Double rankingScore = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime expiredAt = LocalDateTime.now().plusHours(12);

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.UPLOADING;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostFile> postFiles = new ArrayList<>();

    @Column(nullable = false)
    private String authorId;

    private Long dynamicCloudId;

    private Long staticCloudId;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public static Post createInStaticCloud(
        Location location,
        String s2TokenId,
        String content,
        String authorId,
        Long staticCloudId
    ) {
        return Post.builder()
            .location(location)
            .s2TokenId(s2TokenId)
            .content(content)
            .authorId(authorId)
            .staticCloudId(staticCloudId)
            .build();
    }

    public static Post createInDynamicCloud(
        Location location,
        String s2TokenId,
        String content,
        String authorId,
        Long dynamicCloudId
    ) {
        return Post.builder()
            .location(location)
            .s2TokenId(s2TokenId)
            .content(content)
            .authorId(authorId)
            .dynamicCloudId(dynamicCloudId)
            .build();
    }

    public static Post createStandalone(
        Location location,
        String s2TokenId,
        String content,
        String authorId
    ) {
        return Post.builder()
            .location(location)
            .s2TokenId(s2TokenId)
            .content(content)
            .authorId(authorId)
            .build();
    }

    public void assignToDynamicCloud(Long dynamicCloudId) {
        this.dynamicCloudId = dynamicCloudId;
        this.staticCloudId = null;
    }

    public void updateContent(String content) {
        if (content == null) {
            throw new IllegalArgumentException("게시물 내용은 null일 수 없습니다.");
        }
        this.content = content;
    }

    public void updatePostFiles(List<PostFile> postFiles) {
        if (postFiles == null) {
            throw new IllegalArgumentException("게시물 파일은 null일 수 없습니다.");
        }
        this.postFiles.clear();
        addPostFiles(postFiles);
    }
    
    public void addPostFiles(List<PostFile> postFiles) {
        postFiles.forEach(this::addPostFile);
    }

    public void addPostFile(PostFile postFile) {
        this.postFiles.add(postFile);
        postFile.setPost(this);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void markAsActive() {
        this.status = PostStatus.ACTIVE;
    }

    public void markAsUploading() {
        this.status = PostStatus.UPLOADING;
    }

    public void softDeleteByUser() {
        if (this.status == PostStatus.DELETED_BY_USER
            || this.status == PostStatus.DELETED_BY_ADMIN) {
            return;
        }
        this.status = PostStatus.DELETED_BY_USER;
    }

    public void softDeleteByAdmin() {
        if (this.status == PostStatus.DELETED_BY_USER
            || this.status == PostStatus.DELETED_BY_ADMIN) {
            return;
        }
        this.status = PostStatus.DELETED_BY_ADMIN;
    }


    public void increaseLikeCount(long delta) {
        this.likeCount += delta;
        if (this.likeCount < 0) {
            this.likeCount = 0;
        }
    }

    public void increaseDislikeCount(long delta) {
        this.dislikeCount += delta;
        if (this.dislikeCount < 0) {
            this.dislikeCount = 0;
        }
    }
}