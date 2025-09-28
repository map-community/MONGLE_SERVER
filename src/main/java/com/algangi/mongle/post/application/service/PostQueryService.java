package com.algangi.mongle.post.application.service;

import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.presentation.dto.PostDetailResponse;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostFinder postFinder;
    private final PostQueryRepository postQueryRepository;

    public PostListResponse getPostList(PostListRequest request) {
        // TODO: PostQueryRepository를 사용하여 실제 DB에서 데이터 조회 로직 구현 필요.
        // 현재는 임시 데이터를 생성하여 반환합니다.

        List<PostListResponse.PostSummary> mockPosts = IntStream.range(0, request.size())
            .mapToObj(i -> {
                String postId = "post-" + UUID.randomUUID();
                Post mockPost = Post.createStandalone(
                    postId,
                    Location.create(35.890, 128.611),
                    "s2-token-example",
                    "임시 게시글 제목 " + i,
                    "임시 게시글 내용입니다. 필터링 조건: placeId=" + request.placeId() + ", cloudId="
                        + request.cloudId(),
                    1L // 임시 작성자 ID
                );
                return PostListResponse.PostSummary.withMockData(mockPost);
            })
            .toList();

        String lastCursor =
            (mockPosts.size() > 0) ? String.valueOf(System.currentTimeMillis()) : null;

        return PostListResponse.of(mockPosts, lastCursor);
    }

    public PostDetailResponse getPostDetail(String postId) {
        Post post = postFinder.getPostOrThrow(postId);

        // TODO: post.getAuthorId()로 실제 작성자 조회, 파일 URL 생성 등 실제 데이터 기반으로 DTO를 생성해야 합니다.
        // 현재는 임시 데이터를 생성하여 반환합니다.
        return PostDetailResponse.withMockData(post);
    }
}
