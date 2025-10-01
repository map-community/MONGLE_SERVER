package com.algangi.mongle.post.domain.repository;

import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.presentation.dto.PostListRequest;

import java.util.List;
import java.util.Map;

public interface PostQueryRepository {

    List<Post> findPostsByCondition(PostListRequest request, List<String> blockedAuthorIds);

    List<Post> findGrainsInCells(List<String> s2cellTokens, List<String> blockedAuthorIds);

    Map<Long, Long> countPostsByStaticCloudIds(List<Long> cloudIds);

    Map<Long, Long> countPostsByDynamicCloudIds(List<Long> cloudIds);
}