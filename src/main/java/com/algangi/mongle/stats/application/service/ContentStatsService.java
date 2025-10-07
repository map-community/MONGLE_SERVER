package com.algangi.mongle.stats.application.service;

import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.presentation.dto.ReactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
public class ContentStatsService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> reactionScript;
    private final RedisScript<Long> decrementScript;

    private static final String VIEW_COUNT_KEY_PREFIX = "views::";
    private static final String COMMENT_COUNT_KEY_PREFIX = "comments::";
    private static final String LIKES_COUNT_KEY_PREFIX = "likes::";
    private static final String DISLIKES_COUNT_KEY_PREFIX = "dislikes::";
    private static final String REACTIONS_KEY_PREFIX = "reactions::";
    private static final String COMMENT_RANKING_KEY_FORMAT = "comments_by_likes::post::";

    private String getKey(String prefix, TargetType targetType, String targetId) {
        return prefix + targetType.getLowerCase() + "::" + targetId;
    }

    public void incrementPostViewCount(String postId) {
        String key = getKey(VIEW_COUNT_KEY_PREFIX, TargetType.POST, postId);
        redisTemplate.opsForValue().increment(key);
    }

    public void incrementPostCommentCount(String postId) {
        String key = getKey(COMMENT_COUNT_KEY_PREFIX, TargetType.POST, postId);
        redisTemplate.opsForValue().increment(key);
    }

    public void decrementPostCommentCount(String postId) {
        String key = COMMENT_COUNT_KEY_PREFIX + "post::" + postId;
        redisTemplate.execute(decrementScript, List.of(key));
    }

    public void addCommentToRanking(String postId, String commentId) {
        String key = COMMENT_RANKING_KEY_FORMAT + postId;
        redisTemplate.opsForZSet().add(key, commentId, 0);
    }

    public ReactionResponse updateReaction(TargetType targetType, String targetId, String memberId, ReactionType reactionType, String postId) {
        // 1. 리액션 타입 검증
        validateReactionType(reactionType);

        // 2. lua 스크립트에서 쓸 키 & 인자 배열 생성
        List<String> keys = buildReactionKeys(targetType, targetId, postId);
        Object[] args = buildReactionArgs(memberId, reactionType, targetType, targetId);

        // 3. lua 스크립트 실행
        List<Object> result = executeReactionScript(keys, args);

        // 4. lua 스크립트 결과 dto 변환
        return parseReactionResult(result);
    }

    private void validateReactionType(ReactionType reactionType) {
        if (reactionType == null) {
            throw new IllegalArgumentException("reactionType must not be null");
        }
    }

    private List<String> buildReactionKeys(TargetType targetType, String targetId, String postId) {
        List<String> keys = new ArrayList<>();
        keys.add(getKey(REACTIONS_KEY_PREFIX, targetType, targetId));
        keys.add(getKey(LIKES_COUNT_KEY_PREFIX, targetType, targetId));
        keys.add(getKey(DISLIKES_COUNT_KEY_PREFIX, targetType, targetId));

        if (targetType == TargetType.COMMENT) {
            if (!StringUtils.hasText(postId)) {
                throw new IllegalArgumentException("COMMENT 리액션의 경우 postId를 반드시 제공해야 합니다.");
            }
            keys.add(COMMENT_RANKING_KEY_FORMAT + postId);
        }
        return keys;
    }

    private Object[] buildReactionArgs(String memberId, ReactionType reactionType, TargetType targetType, String targetId) {
        return new Object[]{
                memberId,
                reactionType.name(),
                targetType.name(),
                targetId
        };
    }
    private List<Object> executeReactionScript(List<String> keys, Object[] args) {
        List<Object> result = (List<Object>) redisTemplate.execute(reactionScript, keys, args);

        if (result == null || result.size() < 2) {
            throw new IllegalStateException("Lua 스크립트 실행 결과가 올바르지 않습니다.");
        }
        return result;
    }

    private ReactionResponse parseReactionResult(List<Object> result) {
        long likeCount = parseLongSafe(result.get(0));
        long dislikeCount = parseLongSafe(result.get(1));
        return new ReactionResponse(likeCount, dislikeCount);
    }

    private long parseLongSafe(Object value) {
        return (value != null)
                ? Long.parseLong(value.toString())
                : 0L;
    }
}
