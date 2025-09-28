package com.algangi.mongle.post.presentation.dto;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.post.domain.model.Post;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDetailResponse(
    String postId,
    Author author,
    String content,
    double latitude,
    double longitude,
    List<String> photoUrls,
    List<String> videoUrls,
    LocalDateTime createdAt,
    long viewCount,
    long likeCount,
    long dislikeCount,
    long commentCount
) {

    @Builder
    public record Author(
        String id,
        String nickname,
        String profileImageUrl
    ) {

        public static Author from(Member member) {
            // TODO: Member 엔티티에 profileImageUrl 필드 추가 시 반영 필요
            return Author.builder()
                .id(member.getMemberId().toString())
                .nickname(member.getNickname())
                .profileImageUrl("https://i.pravatar.cc/150?u=" + member.getMemberId()) // 임시 URL
                .build();
        }
    }

    public static PostDetailResponse from(Post post, Member author, long commentCount,
        List<String> photoUrls, List<String> videoUrls) {
        return PostDetailResponse.builder()
            .postId(post.getId())
            .author(Author.from(author))
            .content(post.getContent())
            .latitude(post.getLocation().getLatitude())
            .longitude(post.getLocation().getLongitude())
            .photoUrls(photoUrls)
            .videoUrls(videoUrls)
            .createdAt(post.getCreatedDate())
            .viewCount(0) // TODO: 조회수 기능 구현 필요
            .likeCount(post.getLikeCount())
            .dislikeCount(post.getDislikeCount())
            .commentCount(commentCount)
            .build();
    }
}

