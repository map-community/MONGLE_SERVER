package com.algangi.mongle.post.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface PostRepository extends JpaRepository<Post, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    }
    )
    @Query(value = "select p from Post p where p.s2TokenId = :s2TokenId")
    List<Post> findByS2TokenIdWithLock(@Param(value = "s2TokenId") String s2TokenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    }
    )
    @Query(value = "select p from Post p where p.id = :postId")
    Optional<Post> findByPostIdWithLock(@Param(value = "postId") String postId);

    List<Post> findByDynamicCloudIdIn(List<Long> dynamicCloudIds);

    List<Post> findAllByAuthorIdAndStatusIn(String authorId, List<PostStatus> statuses);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query(
        "UPDATE Post p " +
            "SET p.viewCount = p.viewCount + 1 " +
            "WHERE p.id = :postId"
    )
    void incrementViewCount(@Param("postId") String postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p " +
        "SET p.commentCount = p.commentCount - :count " +
        "WHERE p.id = :postId AND p.commentCount >= :count")
    void decrementCommentCount(@Param("postId") String postId, @Param("count") long count);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.status = :status WHERE p.id IN :ids")
    void updateStatusForIds(@Param("ids") List<String> ids, @Param("status") PostStatus status);

    @Query("SELECT p.id FROM Post p WHERE p.authorId = :memberId")
    List<String> findAllIdsByMemberId(@Param("memberId") String memberId);

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query("UPDATE Post p " +
            "SET p.authorId = NULL " +
            "WHERE p.authorId = :memberId")
    int unlinkMemberFromPosts(@Param("memberId") String memberId);
}