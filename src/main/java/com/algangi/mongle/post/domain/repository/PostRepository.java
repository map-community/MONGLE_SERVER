package com.algangi.mongle.post.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.algangi.mongle.post.domain.model.Post;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<Post, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    }
    )
    @Query(value = "select p from Post p where p.s2TokenId = :s2TokenId")
    List<Post> findByS2TokenIdWithLock(@Param(value = "s2TokenId") String s2TokenId);

    List<Post> findByDynamicCloudIdIn(List<Long> dynamicCloudIds);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(
            "UPDATE Post p " +
                    "SET p.viewCount = p.viewCount + 1 " +
                    "WHERE p.id = :postId"
    )
    void incrementViewCount(@Param("postId") String postId);

}
