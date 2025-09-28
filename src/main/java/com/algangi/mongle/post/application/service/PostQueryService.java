package com.algangi.mongle.post.application.service;

import com.algangi.mongle.comment.domain.repository.CommentQueryRepository;
import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.util.DateTimeUtil;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
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
        List<Post> content = postQueryRepository.findPostsByCondition(request);
        if (content.isEmpty()) {
            return PostListResponse.empty();
        }

        Map<String, Long> commentCounts = getCommentCounts(content);
        Map<Long, Member> authors = getAuthors(content);
        Map<String, String> photoUrls = getFirstPhotoUrls(content);

        List<PostListResponse.PostSummary> summaries = content.stream().map(post -> {
            Member author = authors.get(post.getAuthorId());
            long commentCount = commentCounts.getOrDefault(post.getId(), 0L);
            String photoUrl = photoUrls.get(post.getId());
            List<String> photoUrlList =
                photoUrl != null ? List.of(photoUrl) : Collections.emptyList();

            return PostListResponse.PostSummary.from(post, author, commentCount, photoUrlList);
        }).toList();

        String nextCursor = createNextCursor(content,
            request.size() != null && content.size() > request.size(), request.sortBy());

        return new PostListResponse(summaries, nextCursor, nextCursor != null);
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
        return commentQueryRepository.countCommentsByPostIds(postIds);
    }

    private Map<Long, Member> getAuthors(List<Post> posts) {
        List<Long> authorIds = posts.stream().map(Post::getAuthorId).distinct().toList();
        return memberFinder.findMembersByIds(authorIds).stream()
            .collect(Collectors.toMap(Member::getMemberId, Function.identity()));
    }

    private Map<String, String> getFirstPhotoUrls(List<Post> posts) {
        List<String> firstPhotoKeys = posts.stream()
            .map(p -> p.getPostFiles().stream()
                .map(PostFile::getFileKey)
                .filter(key -> key.startsWith("posts/images/"))
                .findFirst()
                .orElse(null))
            .filter(key -> key != null)
            .toList();

        return issueFileUrlsToMap(firstPhotoKeys);
    }

    private List<String> issueFileUrls(List<String> fileKeys) {
        if (fileKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return viewUrlIssueService.issueViewUrls(new ViewUrlRequest(fileKeys)).issuedUrls().stream()
            .map(info -> info.presignedUrl())
            .toList();
    }

    private Map<String, String> issueFileUrlsToMap(List<String> fileKeys) {
        if (fileKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        return viewUrlIssueService.issueViewUrls(new ViewUrlRequest(fileKeys)).issuedUrls().stream()
            .collect(Collectors.toMap(info -> info.fileKey(), info -> info.presignedUrl()));
    }

    private String createNextCursor(List<Post> content, boolean hasNext, PostSort sort) {
        if (!hasNext) {
            return null;
        }

        Post lastPost = content.get(content.size() - 1);
        String formattedDate = lastPost.getCreatedDate().format(DateTimeUtil.CURSOR_DATE_FORMATTER);

        if (sort == PostSort.ranking_score) {
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
