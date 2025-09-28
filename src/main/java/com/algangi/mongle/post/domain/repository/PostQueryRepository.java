package com.algangi.mongle.post.domain.repository;

import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.presentation.dto.PostListRequest;

import java.util.List;

public interface PostQueryRepository {

    /**
     * 게시글 목록을 조건에 따라 동적으로 조회합니다. (정렬, 필터링, 커서 기반 페이징)
     *
     * @param request 필터링 및 정렬 조건
     * @return Post 목록
     */
    List<Post> findPostsByCondition(PostListRequest request);

    /**
     * 주어진 S2 Cell 목록 내에 있으면서, 구름에 속하지 않은 '알갱이' 상태의 게시글을 조회합니다.
     *
     * @param s2TokenIds S2 Cell 토큰 ID 목록
     * @return 알갱이(Post) 목록
     */
    List<Post> findGrainsInCells(List<String> s2TokenIds);
}

