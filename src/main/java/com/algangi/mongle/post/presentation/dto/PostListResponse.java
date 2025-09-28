package com.algangi.mongle.post.presentation.dto;

import com.algangi.mongle.post.domain.model.Post;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PostListResponse(
    List<PostSummary> posts,
    String lastCursor
) {

    public static PostListResponse of(List<PostSummary> posts, String lastCursor) {
        return new PostListResponse(posts, lastCursor);
    }

    @Builder
    public record PostSummary(
        String postId,
        String authorNickname,
        String content,
        List<String> photoUrls,
        List<String> videoUrls,
        long upvotes,
        long downvotes,
        long commentCount,
        LocalDateTime createdAt
    ) {

        public static PostSummary from(Post post, long commentCount) {
            // TODO: 실제 Member 정보를 조회하여 닉네임 설정
            String nickname = "익명의 몽글러";

            // TODO: 실제 PostFile 정보를 기반으로 photoUrls, videoUrls 생성
            List<String> photos = List.of(
                "https://picsum.photos/seed/" + post.getId() + "/400/300");
            List<String> videos = List.of();

            return new PostSummary(
                post.getId(),
                nickname,
                post.getContent(),
                photos,
                videos,
                post.getLikeCount(),
                post.getDislikeCount(),
                commentCount,
                post.getCreatedDate()
            );
        }
    }
}
