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
    String fileKey;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PostFile postFile)) {
            return false;
        }
        return Objects.equals(fileKey, postFile.fileKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fileKey);
    }
}
