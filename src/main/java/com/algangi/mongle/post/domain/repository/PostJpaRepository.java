package com.algangi.mongle.post.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algangi.mongle.post.domain.model.Post;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

}
