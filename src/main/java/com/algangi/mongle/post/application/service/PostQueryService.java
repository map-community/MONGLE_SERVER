package com.algangi.mongle.post.application.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.algangi.mongle.block.application.service.BlockQueryService;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.file.application.dto.PresignedUrl;
import com.algangi.mongle.file.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.util.DateTimeUtil;
import com.algangi.mongle.member.application.service.MemberFinder;
import com.algangi.mongle.member.domain.model.Member;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.model.PostStatus;
import com.algangi.mongle.post.domain.repository.PostQueryRepository;
import com.algangi.mongle.post.event.PostViewedEvent;
import com.algangi.mongle.post.exception.PostErrorCode;
import com.algangi.mongle.post.presentation.dto.PostDetailResponse;
import com.algangi.mongle.post.presentation.dto.PostListRequest;
import com.algangi.mongle.post.presentation.dto.PostListResponse;
import com.algangi.mongle.post.presentation.dto.PostSort;
import com.algangi.mongle.reaction.application.service.ReactionQueryService;
import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;
import com.algangi.mongle.stats.application.dto.PostStats;
import com.algangi.mongle.stats.application.service.ContentStatsService;
import com.algangi.mongle.stats.application.service.StatsQueryService;

import lombok.RequiredArgsConstructor;

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
    private final BlockQueryService blockQueryService;
    private final DynamicCloudRepository dynamicCloudRepository;
    private final StaticCloudRepository staticCloudRepository;
    private final ReactionQueryService reactionQueryService;

    public PostListResponse getPostList(PostListRequest request, String currentMemberId) {
        validateCloudExists(request);

        List<String> blockedAuthorIds = blockQueryService.getBlockedUserIds(currentMemberId);

        List<Post> fetchedPosts = postQueryRepository.findPostsByCondition(request,
            blockedAuthorIds);

        boolean hasNext = fetchedPosts.size() > request.size();
        List<Post> postsOnPage = hasNext ? fetchedPosts.subList(0, request.size()) : fetchedPosts;

        if (postsOnPage.isEmpty()) {
            return PostListResponse.empty();
        }

        List<String> postIds = postsOnPage.stream().map(Post::getId).toList();
        Map<String, PostStats> statsMap = statsQueryService.getPostStatsMap(postIds);
        Map<String, Member> authors = getAuthors(postsOnPage);
        Map<String, List<String>> photoUrlsMap = getPhotoUrlsForPosts(postsOnPage);

        Map<String, ReactionType> myReactionsMap = reactionQueryService.getMyReactions(
            TargetType.POST,
            postIds,
            currentMemberId
        );

        List<PostListResponse.PostSummary> summaries = postsOnPage.stream().map(post -> {
            Member author = authors.get(post.getAuthorId());
            List<String> photoUrlList = photoUrlsMap.getOrDefault(post.getId(),
                Collections.emptyList());
            PostStats stats = statsMap.getOrDefault(post.getId(), PostStats.empty());
            ReactionType myReaction = myReactionsMap.get(post.getId());
            String myReactionStr = (myReaction != null) ? myReaction.name() : null;

            return PostListResponse.PostSummary.from(post, author, photoUrlList, stats,
                myReactionStr);
        }).toList();

        String nextCursor = createNextCursor(postsOnPage, hasNext, request.sortBy());

        return new PostListResponse(summaries, nextCursor, hasNext);
    }

    public PostDetailResponse getPostDetail(String postId, String currentMemberId) {
        Post post = postFinder.getPostOrThrow(postId);

        if (post.getStatus() == PostStatus.DELETED_BY_USER
            || post.getStatus() == PostStatus.DELETED_BY_ADMIN) {
            return PostDetailResponse.deleted();
        }

        Member author = memberFinder.getMemberOrThrow(post.getAuthorId());

        contentStatsService.incrementPostViewCount(postId);
        eventPublisher.publishEvent(new PostViewedEvent(postId));

        PostStats stats = statsQueryService.getPostStatsMap(List.of(postId))
            .getOrDefault(postId, PostStats.empty());

        Map<String, ReactionType> myReactionsMap = reactionQueryService.getMyReactions(
            TargetType.POST,
            List.of(postId),
            currentMemberId
        );
        ReactionType myReaction = myReactionsMap.get(postId);
        String myReactionStr = (myReaction != null) ? myReaction.name() : null;

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

        return PostDetailResponse.from(post, authorDto, stats, photoUrls, videoUrls, myReactionStr);
    }

    private void validateCloudExists(PostListRequest request) {
        try {
            if (StringUtils.hasText(request.cloudId())) {
                long cloudId = Long.parseLong(request.cloudId());
                if (!dynamicCloudRepository.existsById(cloudId)) {
                    throw new ApplicationException(PostErrorCode.CLOUD_NOT_FOUND);
                }
            }
            if (StringUtils.hasText(request.placeId())) {
                long placeId = Long.parseLong(request.placeId());
                if (!staticCloudRepository.existsById(placeId)) {
                    throw new ApplicationException(PostErrorCode.CLOUD_NOT_FOUND);
                }
            }
        } catch (NumberFormatException e) {
            throw new ApplicationException(PostErrorCode.CLOUD_NOT_FOUND, e);
        }
    }

    private Map<String, Member> getAuthors(List<Post> posts) {
        List<String> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return memberFinder.findMembersByIds(authorIds).stream()
            .collect(Collectors.toMap(Member::getMemberId, Function.identity()));
    }

    private Map<String, List<String>> getPhotoUrlsForPosts(List<Post> posts) {
        Map<String, List<String>> postIdToPhotoKeysMap = posts.stream()
            .collect(Collectors.toMap(
                Post::getId,
                p -> p.getPostFiles().stream()
                    .map(PostFile::getFileKey)
                    .filter(key -> key.startsWith("posts/images/"))
                    .toList()
            ));

        List<String> allDistinctPhotoKeys = postIdToPhotoKeysMap.values().stream()
            .flatMap(List::stream)
            .distinct()
            .toList();

        if (allDistinctPhotoKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> photoKeyToUrlMap = issueFileUrlsToMap(allDistinctPhotoKeys);

        Map<String, List<String>> postIdToPhotoUrlsMap = new HashMap<>();
        postIdToPhotoKeysMap.forEach((postId, keys) -> {
            List<String> urls = keys.stream()
                .map(photoKeyToUrlMap::get)
                .filter(Objects::nonNull)
                .toList();
            postIdToPhotoUrlsMap.put(postId, urls);
        });

        return postIdToPhotoUrlsMap;
    }

    private List<String> issueFileUrls(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> distinctFileKeys = fileKeys.stream().distinct().toList();
        if (distinctFileKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return viewUrlIssueService.issueViewUrls(distinctFileKeys)
            .stream()
            .map(PresignedUrl::url)
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
        return viewUrlIssueService.issueViewUrls(distinctFileKeys)
            .stream()
            .collect(
                Collectors.toMap(PresignedUrl::fileKey, PresignedUrl::url, (a, b) -> a));
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