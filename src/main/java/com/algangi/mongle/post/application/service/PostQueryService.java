package com.algangi.mongle.post.application.service;

import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.util.DateTimeUtil;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.application.dto.IssuedUrlInfo;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.model.PostStatus;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.event.PostViewedEvent;
import com.algangi.mongle.post.presentation.dto.PostDetailResponse;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import com.algangi.mongle.post.presentation.dto.PostSort;
import com.algangi.mongle.post.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.stats.application.dto.PostStats;
import com.algangi.mongle.stats.application.service.ContentStatsService;
import com.algangi.mongle.stats.application.service.StatsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final PostQueryRepository postQueryRepository;
    private final ViewUrlIssueService viewUrlIssueService;
    private final ApplicationEventPublisher eventPublisher;
    private final ContentStatsService contentStatsService;
    private final StatsQueryService statsQueryService;

    public PostListResponse getPostList(PostListRequest request) {
        List<Post> fetchedPosts = postQueryRepository.findPostsByCondition(request);
        boolean hasNext = fetchedPosts.size() > request.size();
        List<Post> postsOnPage = hasNext ? fetchedPosts.subList(0, request.size()) : fetchedPosts;

        if (postsOnPage.isEmpty()) {
            return PostListResponse.empty();
        }

        List<String> postIds = postsOnPage.stream().map(Post::getId).toList();
        Map<String, PostStats> statsMap = statsQueryService.getPostStatsMap(postIds);

        Map<String, Member> authors = getAuthors(postsOnPage);
        Map<String, String> photoUrls = getFirstPhotoUrls(postsOnPage);

        List<PostListResponse.PostSummary> summaries = postsOnPage.stream().map(post -> {
            Member author = authors.get(post.getAuthorId());
            String photoUrl = photoUrls.get(post.getId());
            List<String> photoUrlList =
                photoUrl != null ? List.of(photoUrl) : Collections.emptyList();
            PostStats stats = statsMap.getOrDefault(post.getId(), PostStats.empty());

            return PostListResponse.PostSummary.from(post, author, photoUrlList, stats);
        }).toList();

        String nextCursor = createNextCursor(postsOnPage, hasNext, request.sortBy());

        return new PostListResponse(summaries, nextCursor, hasNext);
    }


    public PostDetailResponse getPostDetail(String postId) {
        Post post = postFinder.getPostOrThrow(postId);
        Member author = memberFinder.getMemberOrThrow(post.getAuthorId());

        contentStatsService.incrementPostViewCount(postId);
        eventPublisher.publishEvent(new PostViewedEvent(postId));

        PostStats stats = statsQueryService.getPostStatsMap(List.of(postId))
            .getOrDefault(postId, PostStats.empty());

        String profileImageUrl =
            (post.getStatus() == PostStatus.ACTIVE) ? author.getProfileImage() : null;
        PostDetailResponse.Author authorDto = new PostDetailResponse.Author(
            author.getMemberId(),
            author.getNickname(),
            profileImageUrl
        );

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

        // from 메서드에 author 대신 authorDto를 전달
        return PostDetailResponse.from(post, authorDto, stats, photoUrls, videoUrls);
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
        Map<String, String> postIdToPhotoKeyMap = posts.stream()
            .collect(Collectors.toMap(
                Post::getId,
                p -> p.getPostFiles().stream()
                    .map(PostFile::getFileKey)
                    .filter(key -> key.startsWith("posts/images/"))
                    .findFirst()
                    .orElse(null)
            ));

        List<String> distinctPhotoKeys = postIdToPhotoKeyMap.values().stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (distinctPhotoKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> photoKeyToUrlMap = issueFileUrlsToMap(distinctPhotoKeys);

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