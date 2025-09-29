package com.algangi.mongle.stats.application.service;

import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.presentation.dto.ReactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
public class ContentStatsService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> reactionScript;

    private static final String VIEW_COUNT_KEY_PREFIX = "views::";
    private static final String COMMENT_COUNT_KEY_PREFIX = "comments::";
    private static final String LIKES_COUNT_KEY_PREFIX = "likes::";
    private static final String DISLIKES_COUNT_KEY_PREFIX = "dislikes::";
    private static final String REACTIONS_KEY_PREFIX = "reactions::";

    private String getKey(String prefix, TargetType targetType, String targetId) {
        return prefix + targetType.getLowerCase() + "::" + targetId;
    }

    public Long incrementPostViewCount(String postId) {
        String key = getKey(VIEW_COUNT_KEY_PREFIX, TargetType.POST, postId);
        return redisTemplate.opsForValue().increment(key);
    }

    public void incrementPostCommentCount(String postId) {
        String key = COMMENT_COUNT_KEY_PREFIX + "post::" + postId;
        redisTemplate.opsForValue().increment(key);
    }

    public void decrementPostCommentCount(String postId) {
        String key = COMMENT_COUNT_KEY_PREFIX + "post::" + postId;
        redisTemplate.opsForValue().decrement(key);
    }

    public ReactionResponse updateReaction(TargetType targetType, String targetId, String memberId, ReactionType reactionType) {
        List<String> keys = List.of(
                getKey(REACTIONS_KEY_PREFIX, targetType, targetId),
                getKey(LIKES_COUNT_KEY_PREFIX, targetType, targetId),
                getKey(DISLIKES_COUNT_KEY_PREFIX, targetType, targetId)
        );

        Object[] args = { memberId, reactionType.name() };

        List<Object> result = (List<Object>) redisTemplate.execute(reactionScript, keys, args);

        if (result.size() < 2) {
            throw new IllegalStateException("Lua 스크립트 실행 결과가 올바르지 않습니다.");
        }

        long likeCount = result.get(0) != null
                ? Long.parseLong(result.get(0).toString())
                : 0L;
        long dislikeCount = result.get(1) != null
                ? Long.parseLong(result.get(1).toString())
                : 0L;

        return new ReactionResponse(likeCount, dislikeCount);
    }
}