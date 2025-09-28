package com.algangi.mongle.post.application.service;

import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.presentation.dto.PostDetailResponse;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostFinder postFinder;
    private final MemberFinder memberFinder;
    private final PostQueryRepository postQueryRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final ViewUrlIssueService viewUrlIssueService; // 파일 URL 조회를 위해 주입

    public PostListResponse getPostList(PostListRequest request) {
        // TODO: postQueryRepository를 사용하여 필터링/정렬/커서 기반의 실제 Post 목록 조회 로직 구현 필요.
        // 현재는 임시 데이터를 생성하여 반환합니다.
        List<Post> posts = java.util.stream.IntStream.range(0, request.size())
            .mapToObj(i -> Post.createStandalone(
                "post-" + java.util.UUID.randomUUID(),
                com.algangi.mongle.post.domain.model.Location.create(35.890, 128.611),
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

        List<String> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        Map<String, Long> commentCounts = commentQueryRepository.countCommentsByPostIds(postIds);

        List<PostListResponse.PostSummary> postSummaries = posts.stream()
            .map(post -> {
                long count = commentCounts.getOrDefault(post.getId(), 0L);
                return PostListResponse.PostSummary.from(post, count);
            })
            .toList();

        String lastCursor = (postSummaries.size() >= request.size()) ? "nextCursorExample" : null;

        return PostListResponse.of(postSummaries, lastCursor);
    }

    public PostDetailResponse getPostDetail(String postId) {
        // 1. 게시글 조회 (없으면 예외 발생)
        Post post = postFinder.getPostOrThrow(postId);

        // 2. 작성자 정보 조회 (없으면 예외 발생)
        Member author = memberFinder.getMemberOrThrow(post.getAuthorId());

        // 3. 댓글 수 조회
        long commentCount = commentQueryRepository.countByPostId(postId);

        // 4. 첨부 파일 URL 조회 (임시 데이터)
        // TODO: ViewUrlIssueService를 사용하여 실제 Presigned URL을 생성하는 로직으로 교체해야 합니다.
        //  PostFile 엔티티에 파일 타입(이미지/비디오)을 구분할 수 있는 필드가 필요합니다.
        List<String> photoUrls = post.getPostFiles().stream()
            .map(PostFile::getFileKey)
            // .filter(key -> key.contains("image")) // 실제로는 파일 확장자나 타입으로 구분해야 함
            .map(key -> "https://picsum.photos/seed/" + key.substring(0, 10) + "/400/300")
            .collect(Collectors.toList());

        List<String> videoUrls = post.getPostFiles().stream()
            .map(PostFile::getFileKey)
            // .filter(key -> key.contains("video"))
            .map(key -> "https://example.com/videos/sample_video.mp4")
            .collect(Collectors.toList());

        // 5. 최종 응답 DTO로 변환하여 반환
        return PostDetailResponse.from(post, author, commentCount, photoUrls, videoUrls);
    }
}

