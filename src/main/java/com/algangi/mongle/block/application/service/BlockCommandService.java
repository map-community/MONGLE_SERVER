package com.algangi.mongle.block.application.service;

import com.algangi.mongle.block.domain.model.Block;
import com.algangi.mongle.block.domain.repository.BlockRepository;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockCommandService {

    private final BlockRepository blockRepository;
    private final MemberFinder memberFinder;

    public void blockUser(String blockerId, String blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("자기 자신을 차단할 수 없습니다.");
        }

        blockRepository.findByBlocker_MemberIdAndBlocked_MemberId(blockerId, blockedId)
                .ifPresent(block -> { return; });

        Member blocker = memberFinder.getMemberOrThrow(blockerId);
        Member blocked = memberFinder.getMemberOrThrow(blockedId);

        Block newBlock = Block.of(blocker, blocked);
        blockRepository.save(newBlock);
    }

    public void unblockUser(String blockerId, String blockedId) {
        blockRepository.findByBlocker_MemberIdAndBlocked_MemberId(blockerId, blockedId)
                .ifPresent(blockRepository::delete);
    }
}
