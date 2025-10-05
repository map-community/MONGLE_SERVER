package com.algangi.mongle.post.domain.model;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.global.annotation.ULID;
import com.algangi.mongle.global.entity.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection
    @CollectionTable(name = "post_file",
        joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "list_idx")
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
        String postId,
        Location location,
        String s2TokenId,
        String content,
        String authorId,
        Long staticCloudId
    ) {
        return Post.builder()
            .id(postId)
            .location(location)
            .s2TokenId(s2TokenId)
            .content(content)
            .authorId(authorId)
            .staticCloudId(staticCloudId)
            .build();
    }

    public static Post createInDynamicCloud(
        String postId,
        Location location,
        String s2TokenId,
        String content,
        String authorId,
        Long dynamicCloudId
    ) {
        return Post.builder()
            .id(postId)
            .location(location)
            .s2TokenId(s2TokenId)
            .content(content)
            .authorId(authorId)
            .dynamicCloudId(dynamicCloudId)
            .build();
    }

    public static Post createStandalone(
        String postId,
        Location location,
        String s2TokenId,
        String content,
        String authorId
    ) {
        return Post.builder()
            .id(postId)
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

    public void addPostFiles(List<PostFile> postFiles) {
        this.postFiles.addAll(postFiles);
    }

    public void changePostFiles(List<PostFile> postFiles) {
        this.postFiles.clear();
        this.postFiles.addAll(postFiles);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void markAsActive() {
        this.status = PostStatus.ACTIVE;
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