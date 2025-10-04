package com.algangi.mongle.post.presentation.dto;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.stats.application.dto.PostStats;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record PostDetailResponse(
    String postId,
    Author author,
    String content,
    double latitude,
    double longitude,
    List<String> photoUrls,
    List<String> videoUrls,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    long viewCount,
    long likeCount,
    long dislikeCount,
    long commentCount
) {

    public record Author(
        String id,
        String nickname,
        String profileImageUrl
    ) {

        public static Author from(Member member) {
            if (member == null) {
                return new Author(null, "익명의 몽글러", null);
            }
            return new Author(
                member.getMemberId(),
                member.getNickname(),
                member.getProfileImage()
            );
        }
    }

    public static PostDetailResponse from(Post post, Author authorDto, PostStats stats,
        List<String> photoUrls, List<String> videoUrls) {
        return new PostDetailResponse(
            post.getId(),
            authorDto,
            post.getContent(),
            post.getLocation().getLatitude(),
            post.getLocation().getLongitude(),
            photoUrls,
            videoUrls,
            post.getCreatedDate(),
            post.getUpdatedDate(),
            stats.viewCount(),
            post.getLikeCount(),
            post.getDislikeCount(),
            stats.commentCount()
        );
    }

    public static PostDetailResponse deleted() {
        return new PostDetailResponse(
            null,
            new Author(null, "알 수 없음", null),
            "삭제된 게시물입니다.",
            0.0,
            0.0,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            null,
            0,
            0,
            0,
            0
        );
    }
}