package com.algangi.mongle.comment.domain.repository;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.model.CommentStatus;
import com.algangi.mongle.post.domain.model.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {
    @Query("SELECT c " +
            "FROM Comment c JOIN FETCH c.post p " +
            "WHERE c.member.memberId = :memberId AND p.status IN :statuses")
    List<Comment> findAllByMemberIdAndPostStatusIn(@Param("memberId") String memberId, @Param("statuses") List<PostStatus> statuses);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c " +
            "SET c.status = :status " +
            "WHERE c.id IN :ids")
    void updateStatusForIds(@Param("ids") List<String> ids, @Param("status") CommentStatus status);

    List<Comment> findAllByMember_MemberId(String memberId);

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query("UPDATE Comment c " +
            "SET c.member = NULL " +
            "WHERE c.member.memberId = :memberId")
    int unlinkMemberFromComments(@Param("memberId") String memberId);
}
