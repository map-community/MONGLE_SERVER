package com.algangi.mongle.post.application.service;

import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostFinder postFinder;
    private final PostQueryRepository postQueryRepository;
    private final CommentQueryRepository commentQueryRepository;

    public PostListResponse getPostList(PostListRequest request) {
        // TODO: postQueryRepository를 사용하여 필터링/정렬/커서 기반의 실제 Post 목록 조회 로직 구현 필요.
        // 현재는 임시 데이터를 생성하여 반환합니다.
        List<Post> posts = IntStream.range(0, request.size())
            .mapToObj(i -> Post.createStandalone(
                "post-" + UUID.randomUUID(),
                Location.create(35.890, 128.611),
                "s2-token-example",
                "임시 게시글 제목 " + i,
                "임시 게시글 내용입니다. 필터링 조건: placeId=" + request.placeId() + ", cloudId="
                    + request.cloudId(),
                1L // 임시 작성자 ID
            ))
            .toList();

        if (posts.isEmpty()) {
            return PostListResponse.of(Collections.emptyList(), null);
        }

        // 1. 조회된 게시글 목록의 ID 추출
        List<String> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // 2. 게시글 ID 목록으로 댓글 수 Map을 한 번에 조회 (N+1 방지)
        Map<String, Long> commentCounts = commentQueryRepository.countCommentsByPostIds(postIds);

        // 3. Post와 댓글 수를 매핑하여 최종 DTO 생성
        List<PostListResponse.PostSummary> postSummaries = posts.stream()
            .map(post -> {
                long count = commentCounts.getOrDefault(post.getId(), 0L);
                return PostListResponse.PostSummary.from(post, count);
            })
            .toList();

        // TODO: 실제 커서 로직 구현 필요
        String lastCursor = (postSummaries.size() >= request.size()) ? "nextCursorExample" : null;

        return PostListResponse.of(postSummaries, lastCursor);
    }
}

