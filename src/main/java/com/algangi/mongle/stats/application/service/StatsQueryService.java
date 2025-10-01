package com.algangi.mongle.stats.application.service;

import com.algangi.mongle.comment.application.vo.CommentStats;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsQueryService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LIKE_KEY_FORMAT = "likes::comment::%s";
    private static final String DISLIKE_KEY_FORMAT = "dislikes::comment::%s";

    public Map<String, CommentStats> getCommentStatsMap(List<String> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 각 통계 유형별 키 목록 생성
        List<String> likeKeys = buildKeys(commentIds, LIKE_KEY_FORMAT);
        List<String> dislikeKeys = buildKeys(commentIds, DISLIKE_KEY_FORMAT);

        // MGET을 위해 모든 키를 하나의 리스트로 합침
        List<String> results = redisTemplate.opsForValue()
                .multiGet(concatLists(likeKeys, dislikeKeys));

        if (results == null) {
            results = Collections.emptyList();
        }

        // 결과를 Map<commentId, CommentStats> 형태로 가공
        Map<String, CommentStats> statsMap = new HashMap<>();
        for (int i = 0; i < commentIds.size(); i++) {
            long likeCount = safeParseLong(results, i);
            long dislikeCount = safeParseLong(results, i + commentIds.size());
            statsMap.put(commentIds.get(i), new CommentStats(likeCount, dislikeCount));
        }
        return statsMap;
    }

    private List<String> buildKeys(List<String> ids, String format) {
        return ids.stream()
                .map(id -> String.format(format, id))
                .toList();
    }

    private List<String> concatLists(List<String> first, List<String> second) {
        List<String> merged = new ArrayList<>(first.size() + second.size());
        merged.addAll(first);
        merged.addAll(second);
        return merged;
    }

    private long safeParseLong(List<String> results, int index) {
        if (index >= results.size()) return 0L;
        String value = results.get(index);
        if (value == null) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}