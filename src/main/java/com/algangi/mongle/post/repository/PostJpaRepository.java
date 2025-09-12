package com.algangi.mongle.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.algangi.mongle.post.domain.Post;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

}
