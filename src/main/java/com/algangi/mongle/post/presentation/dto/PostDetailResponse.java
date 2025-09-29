package com.algangi.mongle.post.presentation.dto;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.post.domain.model.Post;

import java.time.LocalDateTime;
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
    long viewCount, // TODO: 조회수 기능 구현 필요
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
                member.getMemberId().toString(),
                member.getNickname(),
                member.getProfileImage()
            );
        }
    }

    public static PostDetailResponse from(Post post, Member author, long commentCount,
        List<String> photoUrls, List<String> videoUrls) {
        return new PostDetailResponse(
            post.getId(),
            Author.from(author),
            post.getContent(),
            post.getLocation().getLatitude(),
            post.getLocation().getLongitude(),
            photoUrls,
            videoUrls,
            post.getCreatedDate(),
            0, // TODO: 조회수 기능 구현 후 반영
            post.getLikeCount(),
            post.getDislikeCount(),
            commentCount
        );
    }
}

