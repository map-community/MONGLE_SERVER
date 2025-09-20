package com.algangi.mongle.post.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.algangi.mongle.post.domain.model.Post;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    }
    )
    @Query(value = "select p from Post p where p.s2TokenId = :s2TokenId")
    List<Post> findByS2TokenIdWithLock(@Param(value = "s2TokenId") String s2TokenId);

    List<Post> findByDynamicCloudIdIn(List<Long> dynamicCloudIds);

}
