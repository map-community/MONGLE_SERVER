package com.algangi.mongle.post.domain.repository;

import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.presentation.dto.PostListRequest;

import java.util.List;
import java.util.Map;

public interface PostQueryRepository {

    /**
     * 조건에 맞는 게시글 목록을 커서 기반으로 조회합니다.
     */
    List<Post> findPostsByCondition(PostListRequest request);

    /**
     * 주어진 S2 Cell 목록 내에 '알갱이' 상태로 존재하는 게시글들을 조회합니다.
     */
    List<Post> findGrainsInCells(List<String> s2cellTokens);

    /**
     * 주어진 정적 구름 ID 목록에 대해 각 구름에 속한 게시글 수를 조회합니다.
     *
     * @return Map<CloudId, PostCount>
     */
    Map<Long, Long> countPostsByStaticCloudIds(List<Long> cloudIds);

    /**
     * 주어진 동적 구름 ID 목록에 대해 각 구름에 속한 게시글 수를 조회합니다.
     *
     * @return Map<CloudId, PostCount>
     */
    Map<Long, Long> countPostsByDynamicCloudIds(List<Long> cloudIds);
}

