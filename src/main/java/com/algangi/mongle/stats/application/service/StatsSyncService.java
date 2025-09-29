package com.algangi.mongle.stats.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsSyncService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final Map<String, Set<String>> ALLOWED_COLUMNS = Map.of(
            "post", Set.of("view_count", "comment_count", "like_count", "dislike_count"),
            "comment", Set.of("like_count", "dislike_count")
    );

    private static final int CHUNK_SIZE = 1000;

    @Transactional
    public void syncCounts(String pattern, String tableName, String columnName) {
        if (!isAllowedColumn(tableName, columnName)) {
            return;
        }

        List<Object[]> batchArgs = new ArrayList<>();

        try (Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(pattern).count(1000).build())) {

            while (cursor.hasNext()) {
                String key = cursor.next();
                processKey(key, tableName, columnName, batchArgs);
            }
        }

        flushRemainingBatch(batchArgs, tableName, columnName);
    }

    private boolean isAllowedColumn(String tableName, String columnName) {
        if (!ALLOWED_COLUMNS.containsKey(tableName) || !ALLOWED_COLUMNS.get(tableName).contains(columnName)) {
            log.error("[CRITICAL] 허용되지 않은 테이블/컬럼 접근 시도 차단: {}.{}", tableName, columnName);
            return false;
        }
        return true;
    }

    private void processKey(String key, String tableName, String columnName, List<Object[]> batchArgs) {
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) return;

        Long count = parseCount(key, countStr);
        String id = extractId(key);

        if (count == null || !StringUtils.hasText(id) || count < 0) {
            log.warn("유효하지 않은 통계 데이터 감지. key='{}', count={}, id='{}'", key, countStr, id);
            return;
        }

        addToBatch(batchArgs, tableName, columnName, count, id);
    }

    private Long parseCount(String key, String countStr) {
        try {
            return Long.parseLong(countStr);
        } catch (NumberFormatException e) {
            log.warn("Redis 키 '{}'의 값이 숫자 형식이 아닙니다. 값: {}", key, countStr);
            return null;
        }
    }

    private String extractId(String key) {
        String[] parts = key.split("::");
        if (parts.length < 2) {
            log.warn("Redis 키 '{}'의 형식이 올바르지 않습니다.", key);
            return null;
        }
        return parts[parts.length - 1];
    }

    private void addToBatch(List<Object[]> batchArgs, String tableName, String columnName, long count, String id) {
        batchArgs.add(new Object[]{count, id});
        if (batchArgs.size() >= CHUNK_SIZE) {
            flushBatch(tableName, columnName, batchArgs);

            batchArgs.clear();
        }
    }

    private void flushRemainingBatch(List<Object[]> batchArgs, String tableName, String columnName) {
        if (!batchArgs.isEmpty()) {
            flushBatch(tableName, columnName, batchArgs);
        }
    }

    private void flushBatch(String tableName, String columnName, List<Object[]> batchArgs) {
        String sql = String.format("UPDATE %s SET %s = ? WHERE id = ?", tableName, columnName);
        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.debug("{} 테이블의 {} 컬럼에 {}건의 데이터를 동기화했습니다.", tableName, columnName, batchArgs.size());
    }
}