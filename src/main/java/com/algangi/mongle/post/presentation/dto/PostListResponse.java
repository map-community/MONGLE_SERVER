package com.algangi.mongle.post.presentation.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.algangi.mongle.member.domain.model.Member;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostStatus;
import com.algangi.mongle.stats.application.dto.PostStats;


import lombok.Getter;

@Getter
public class PostListResponse {

    private final List<PostSummary> posts;
    private final String nextCursor;
    private final boolean hasNext;

    public PostListResponse(List<PostSummary> posts, String nextCursor, boolean hasNext) {
        this.posts = posts;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

    public static PostListResponse empty() {
        return new PostListResponse(Collections.emptyList(), null, false);
    }

    public record PostSummary(
        String postId,
        Author author,
        String content,
        List<String> photoUrls,
        long upvotes,
        long downvotes,
        String myReaction,
        long commentCount,
        long viewCount,
        Instant createdAt,
        Instant updatedAt
    ) {

        public static PostSummary from(Post post, Member author, List<String> photoUrls,
            PostStats stats, String myReaction) {
            // 게시글 상태가 ACTIVE일 때만 프로필 이미지 URL을 사용, 아니면 null
            String profileImageUrl = (post.getStatus() == PostStatus.ACTIVE && author != null)
                ? author.getProfileImage()
                : null;

            Author authorDto = (author != null)
                ? new Author(author.getMemberId(), author.getNickname(), profileImageUrl)
                : new Author(null, "익명의 몽글러", null);

            return new PostSummary(
                post.getId(),
                authorDto,
                post.getContent(),
                photoUrls,
                stats.likeCount(),
                stats.dislikeCount(),
                myReaction,
                stats.commentCount(),
                stats.viewCount(),
                post.getCreatedDate(),
                post.getUpdatedDate()
            );
        }

        public record Author(
            String id,
            String nickname,
            String profileImageUrl
        ) {

        }
    }
}