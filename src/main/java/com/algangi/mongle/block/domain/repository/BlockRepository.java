package com.algangi.mongle.block.domain.repository;

import com.algangi.mongle.block.domain.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, String> {

    Optional<Block> findByBlocker_MemberIdAndBlocked_MemberId(String blockerId, String blockedId);

    @Query("SELECT b.blocked.memberId FROM Block b WHERE b.blocker.memberId = :blockerId")
    List<String> findBlockedMemberIdsByBlockerId(@Param("blockerId") String blockerId);
}