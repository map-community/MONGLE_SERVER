package com.algangi.mongle.post.domain.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class PostFile {

    @Column(nullable = false)
    String fileUrl;

    @Column(nullable = false)
    String s3Key;

    public static PostFile create(String fileUrl, String s3Key) {
        validatePostFile(fileUrl, s3Key);
        return PostFile.builder()
            .fileUrl(fileUrl)
            .s3Key(s3Key)
            .build();
    }

    private static void validatePostFile(String fileUrl, String s3Key) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("fileUrl cannot be null or empty");
        }
        if (s3Key == null || s3Key.isEmpty()) {
            throw new IllegalArgumentException("s3Key cannot be null or empty");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PostFile postFile)) {
            return false;
        }
        return Objects.equals(fileUrl, postFile.fileUrl) && Objects.equals(s3Key,
            postFile.s3Key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileUrl, s3Key);
    }
}
