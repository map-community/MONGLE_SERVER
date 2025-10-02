package com.algangi.mongle.reaction.domain.repository;

import com.algangi.mongle.reaction.domain.model.Reaction;
import com.algangi.mongle.reaction.domain.model.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, String> {

    Optional<Reaction> findByMember_MemberIdAndTargetIdAndTargetType(
            String memberId, String targetId, TargetType targetType
    );

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Reaction r " +
            "WHERE r.targetType = :targetType AND r.targetId IN :targetIds")
    void deleteAllByTargetTypeAndTargetIdIn(@Param("targetType") TargetType targetType, @Param("targetIds") List<String> targetIds);
}