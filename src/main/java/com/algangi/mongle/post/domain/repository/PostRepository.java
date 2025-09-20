package com.algangi.mongle.post.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algangi.mongle.post.domain.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByS2TokenId(String s2TokenId);

    List<Post> findByDynamicCloudIdIn(List<Long> dynamicCloudIds);

}
