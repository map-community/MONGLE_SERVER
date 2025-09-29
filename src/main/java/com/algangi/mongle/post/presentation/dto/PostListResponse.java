package com.algangi.mongle.post.presentation.dto;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.post.domain.model.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
        String authorNickname,
        String content,
        List<String> photoUrls,
        long upvotes,
        long downvotes,
        long commentCount,
        LocalDateTime createdAt
    ) {

        public static PostSummary from(Post post, Member author, long commentCount,
            List<String> photoUrls) {
            String nickname =
                (author != null) ? author.getNickname() : "익명의 몽글러"; // author가 null일 경우 대비

            return new PostSummary(
                post.getId(),
                nickname,
                post.getContent(),
                photoUrls,
                post.getLikeCount(),
                post.getDislikeCount(),
                commentCount,
                post.getCreatedDate()
            );
        }
    }
}

