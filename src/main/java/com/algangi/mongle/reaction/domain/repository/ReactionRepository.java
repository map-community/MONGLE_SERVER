package com.algangi.mongle.reaction.domain.repository;

import com.algangi.mongle.reaction.domain.model.Reaction;
import com.algangi.mongle.reaction.domain.model.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, String> {

    Optional<Reaction> findByMember_MemberIdAndTargetIdAndTargetType(
            String memberId, String targetId, TargetType targetType
    );
}