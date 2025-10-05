package com.algangi.mongle.post.domain.model;

import com.algangi.mongle.global.annotation.ULID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class PostFile {

    @Id
    @ULID
    private String id;

    @Column(nullable = false)
    private String fileKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static PostFile create(String fileKey) {
        validatePostFile(fileKey);
        return PostFile.builder()
            .fileKey(fileKey)
            .build();
    }

    private static void validatePostFile(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            throw new IllegalArgumentException("fileKey는 null이나 빈 값일 수 없습니다.");
        }
    }

    protected void setPost(Post post) {
        this.post = post;
    }

}
