package com.algangi.mongle.stats.application.service;

import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.presentation.dto.ReactionResponse;
import com.algangi.mongle.stats.application.dto.ReactionCleanupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public void removeReactionsFromRedis(String memberId, List<ReactionCleanupDto> reactions) {
        if (!StringUtils.hasText(memberId) || reactions == null || reactions.isEmpty()) {
            log.warn("Invalid arguments for removeReactionsFromRedis. memberId: {}, reactions is empty: {}",
                    memberId, reactions == null || reactions.isEmpty());
            return;
        }

        var serializer = redisTemplate.getStringSerializer();
        List<Object> results = null;

        try {
            results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (ReactionCleanupDto reaction : reactions) {
                    String targetId = reaction.targetId();
                    ReactionType reactionType = reaction.reactionType();

                    // reactions hash에서 해당 사용자 필드 삭제
                    byte[] reactionsKey = serializer.serialize(
                            getKey(REACTIONS_KEY_PREFIX, reaction.targetType(), targetId)
                    );
                    byte[] memberField = serializer.serialize(memberId);
                    connection.hashCommands().hDel(reactionsKey, memberField);

                    // 좋아요/싫어요 카운트 감소
                    if (reactionType == ReactionType.LIKE) {
                        byte[] likesKey = serializer.serialize(
                                getKey(LIKES_COUNT_KEY_PREFIX, reaction.targetType(), targetId)
                        );
                        connection.stringCommands().decrBy(likesKey, 1);
                    } else if (reactionType == ReactionType.DISLIKE) {
                        byte[] dislikesKey = serializer.serialize(
                                getKey(DISLIKES_COUNT_KEY_PREFIX, reaction.targetType(), targetId)
                        );
                        connection.stringCommands().decrBy(dislikesKey, 1);
                    }

                    // 댓글인 경우 랭킹 ZSET에서 제거
                    if (reaction.targetType() == TargetType.COMMENT
                            && reactionType == ReactionType.LIKE) {
                        String postId = reaction.postId();
                        if (StringUtils.hasText(postId)) {
                            byte[] rankingKey = serializer.serialize(
                                    COMMENT_RANKING_KEY_FORMAT + postId
                            );
                            connection.zSetCommands().zIncrBy(rankingKey, -1,
                                    serializer.serialize(targetId));
                        }
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to execute Redis pipeline for member withdrawal: {}", memberId, e);
            throw new RuntimeException("Redis pipeline execution failed", e);
        }

        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                Object result = results.get(i);
                if (result instanceof Exception) {
                    // 파이프라인 내부의 특정 명령어가 실패한 경우
                    log.error("A command in the withdrawal pipeline failed for memberId {}. Result index: {}. Error: {}",
                            memberId, i, ((Exception) result).getMessage());
                } else if (result instanceof Long && (Long) result < 0) {
                    // decrBy 실행 후 카운트가 음수가 된 경우
                    log.warn("Redis count became negative after decrement for memberId {}. Result index: {}. Value: {}",
                            memberId, i, result);
                }
            }
        }
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
