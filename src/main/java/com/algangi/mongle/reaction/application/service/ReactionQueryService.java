package com.algangi.mongle.reaction.application.service;

import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
public class ReactionQueryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> getReactionsScript;

    private static final String REACTIONS_KEY_PREFIX = "reactions::";

    private String getKey(TargetType targetType, String targetId) {
        return REACTIONS_KEY_PREFIX + targetType.getLowerCase() + "::" + targetId;
    }

    public Map<String, ReactionType> getMyReactions(TargetType targetType, List<String> targetIds, String memberId) {
        if (memberId == null || memberId.isBlank() || targetIds == null || targetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> keys = targetIds.stream()
                .map(targetId -> getKey(targetType, targetId))
                .toList();

        List<String> results = (List<String>) redisTemplate.execute(getReactionsScript, keys, memberId);

        if (results == null) {
            return Collections.emptyMap();
        }

        Map<String, ReactionType> reactionMap = new HashMap<>();
        IntStream.range(0, targetIds.size()).forEach(i -> {
            String targetId = targetIds.get(i);
            String reactionStr = results.get(i);

            if (reactionStr != null) {
                reactionMap.put(targetId, ReactionType.valueOf(reactionStr));
            }
        });

        return reactionMap;
    }
}