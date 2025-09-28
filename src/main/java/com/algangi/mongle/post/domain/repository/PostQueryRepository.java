package com.algangi.mongle.post.domain.repository;

import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.presentation.dto.PostListRequest;

import java.util.List;

public interface PostQueryRepository {

    /**
     * 조건에 따라 게시글 목록을 조회하는 동적 쿼리
     *
     * @param request 필터링, 정렬, 커서, 페이지 사이즈 정보
     * @return 조회된 Post 목록
     */
    List<Post> findPostsByCondition(PostListRequest request);

}

