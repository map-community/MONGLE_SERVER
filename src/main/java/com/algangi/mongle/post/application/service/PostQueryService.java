package com.algangi.mongle.post.application.service;

import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.util.DateTimeUtil;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.application.dto.IssuedUrlInfo;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.presentation.dto.PostDetailResponse;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import com.algangi.mongle.post.presentation.dto.PostSort;
import com.algangi.mongle.post.presentation.dto.ViewUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostFinder postFinder;
    private final MemberFinder memberFinder;
    private final CommentQueryRepository commentQueryRepository;
    private final PostQueryRepository postQueryRepository;
    private final ViewUrlIssueService viewUrlIssueService;


    public PostListResponse getPostList(PostListRequest request) {
        // 1. DB에서 다음 페이지 존재 여부 확인을 위해 요청 사이즈보다 1개 더 조회
        List<Post> fetchedPosts = postQueryRepository.findPostsByCondition(request);
        boolean hasNext = fetchedPosts.size() > request.size();
        // 2. 실제 페이지에 해당하는 데이터만 잘라냄 (가장 중요한 리팩토링 포인트)
        List<Post> postsOnPage = hasNext ? fetchedPosts.subList(0, request.size()) : fetchedPosts;

        if (postsOnPage.isEmpty()) {
            return PostListResponse.empty();
        }

        // 3. 잘라낸 'postsOnPage'를 기준으로 후속 작업을 처리하여 불필요한 연산을 방지
        Map<String, Long> commentCounts = getCommentCounts(postsOnPage);
        Map<String, Member> authors = getAuthors(postsOnPage);
        Map<String, String> photoUrls = getFirstPhotoUrls(postsOnPage); // postId -> URL 맵

        // 4. DTO 조립
        List<PostListResponse.PostSummary> summaries = postsOnPage.stream().map(post -> {
            Member author = authors.get(post.getAuthorId());
            long commentCount = commentCounts.getOrDefault(post.getId(), 0L);
            String photoUrl = photoUrls.get(post.getId());
            List<String> photoUrlList =
                photoUrl != null ? List.of(photoUrl) : Collections.emptyList();

            return PostListResponse.PostSummary.from(post, author, commentCount, photoUrlList);
        }).toList();

        String nextCursor = createNextCursor(postsOnPage, hasNext, request.sortBy());

        return new PostListResponse(summaries, nextCursor, hasNext);
    }

    public PostDetailResponse getPostDetail(String postId) {
        Post post = postFinder.getPostOrThrow(postId);
        Member author = memberFinder.getMemberOrThrow(post.getAuthorId());
        long commentCount = commentQueryRepository.countByPostId(postId);

        List<String> photoKeys = post.getPostFiles().stream()
            .map(PostFile::getFileKey)
            .filter(key -> key.startsWith("posts/images/"))
            .toList();
        List<String> videoKeys = post.getPostFiles().stream()
            .map(PostFile::getFileKey)
            .filter(key -> key.startsWith("posts/videos/"))
            .toList();

        List<String> photoUrls = issueFileUrls(photoKeys);
        List<String> videoUrls = issueFileUrls(videoKeys);

        // TODO: 조회수(viewCount)는 추후 구현 필요
        return PostDetailResponse.from(post, author, commentCount, photoUrls, videoUrls);
    }


    private Map<String, Long> getCommentCounts(List<Post> posts) {
        List<String> postIds = posts.stream().map(Post::getId).toList();
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentQueryRepository.countCommentsByPostIds(postIds);
    }

    private Map<String, Member> getAuthors(List<Post> posts) {
        List<String> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return memberFinder.findMembersByIds(authorIds).stream()
            .collect(Collectors.toMap(Member::getMemberId, Function.identity()));
    }

    private Map<String, String> getFirstPhotoUrls(List<Post> posts) {
        // 1. postId를 Key로, 첫 번째 이미지의 fileKey를 Value로 갖는 Map을 생성합니다.
        // 이미지가 없는 게시글의 경우 Value는 null이 됩니다.
        Map<String, String> postIdToPhotoKeyMap = posts.stream()
            .collect(Collectors.toMap(
                Post::getId,
                p -> p.getPostFiles().stream()
                    .map(PostFile::getFileKey)
                    .filter(key -> key.startsWith("posts/images/"))
                    .findFirst()
                    .orElse(null)
            ));

        // 2. 이미지가 있는 게시글의 fileKey 목록만 추출하여 중복을 제거합니다.
        List<String> distinctPhotoKeys = postIdToPhotoKeyMap.values().stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (distinctPhotoKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        // 3. 추출된 fileKey 목록으로 Presigned URL을 한 번에 요청합니다.
        Map<String, String> photoKeyToUrlMap = issueFileUrlsToMap(distinctPhotoKeys);

        // 4. 최종적으로 postId를 Key로, Presigned URL을 Value로 갖는 Map을 생성하여 반환합니다.
        return postIdToPhotoKeyMap.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> photoKeyToUrlMap.get(entry.getValue())
            ));
    }


    private List<String> issueFileUrls(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> distinctFileKeys = fileKeys.stream().distinct().toList();
        if (distinctFileKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return viewUrlIssueService.issueViewUrls(new ViewUrlRequest(distinctFileKeys)).issuedUrls()
            .stream()
            .map(IssuedUrlInfo::presignedUrl)
            .toList();
    }

    private Map<String, String> issueFileUrlsToMap(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> distinctFileKeys = fileKeys.stream().distinct().toList();
        if (distinctFileKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        return viewUrlIssueService.issueViewUrls(new ViewUrlRequest(distinctFileKeys)).issuedUrls()
            .stream()
            .collect(
                Collectors.toMap(IssuedUrlInfo::fileKey, IssuedUrlInfo::presignedUrl, (a, b) -> a));
    }

    private String createNextCursor(List<Post> content, boolean hasNext, PostSort sort) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }

        Post lastPost = content.get(content.size() - 1);
        String formattedDate = lastPost.getCreatedDate().format(DateTimeUtil.CURSOR_DATE_FORMATTER);

        PostSort finalSort = (sort == null) ? PostSort.ranking_score : sort;

        if (finalSort == PostSort.ranking_score) {
            return String.join("_",
                String.valueOf(lastPost.getRankingScore()),
                formattedDate,
                lastPost.getId());
        } else {
            return String.join("_",
                formattedDate,
                lastPost.getId());
        }
    }
}
