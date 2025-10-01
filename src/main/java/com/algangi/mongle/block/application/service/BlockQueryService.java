package com.algangi.mongle.block.application.service;

import com.algangi.mongle.block.domain.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockQueryService {

    private final BlockRepository blockRepository;

    public List<String> getBlockedUserIds(String blockerId) {
        return blockRepository.findBlockedMemberIdsByBlockerId(blockerId);
    }
}
