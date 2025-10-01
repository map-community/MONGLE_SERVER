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

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsSyncService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final int CHUNK_SIZE = 1000;

    @Transactional
    public void syncPostCommentCountsToDb() {
        log.info("[Sync] Redis -> DB :: 게시물 댓글 수 동기화를 시작합니다.");

        String pattern = "comments::post::*";
        List<Object[]> batchArgs = new ArrayList<>();

        try (Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(pattern).count(1000).build()
        )) {
            processCursor(cursor, batchArgs);
        }

        flushRemainingBatch(batchArgs);

        log.info("[Sync] Redis -> DB :: 게시물 댓글 수 동기화를 완료했습니다.");
    }

    private void processCursor(Cursor<String> cursor, List<Object[]> batchArgs) {
        while (cursor.hasNext()) {
            String key = cursor.next();
            String countStr = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(countStr)) continue;

            String postId = extractIdFromKey(key);
            addToBatch(batchArgs, postId, countStr, key);
        }
    }

    private String extractIdFromKey(String key) {
        String[] parts = key.split("::");
        return parts[parts.length - 1];
    }

    private void addToBatch(List<Object[]> batchArgs, String postId, String countStr, String key) {
        try {
            long count = Long.parseLong(countStr);
            batchArgs.add(new Object[]{count, postId});

            if (batchArgs.size() >= CHUNK_SIZE) {
                flushBatchUpdateToDb("post", "comment_count", batchArgs);
                batchArgs.clear();
            }
        } catch (NumberFormatException e) {
            log.warn("Redis 키 '{}'의 값이 숫자 형식이 아닙니다. 값: {}", key, countStr);
        }
    }

    private void flushBatchUpdateToDb(String tableName, String columnName, List<Object[]> batchArgs) {
        String sql = String.format("UPDATE %s SET %s = ? WHERE id = ?", tableName, columnName);
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void flushRemainingBatch(List<Object[]> batchArgs) {
        if (!batchArgs.isEmpty()) {
            flushBatchUpdateToDb("post", "comment_count", batchArgs);
            batchArgs.clear();
        }
    }

    @Transactional(readOnly = true)
    public void syncReactionCountsToRedis() {
        log.info("[Sync] DB -> Redis :: 반응(좋아요/싫어요) 수 동기화를 시작합니다.");

        // 게시물 좋아요/싫어요 동기화
        syncCountsFromDbToRedis("post", "like_count", "likes::post::%s");
        syncCountsFromDbToRedis("post", "dislike_count", "dislikes::post::%s");

        // 댓글 좋아요/싫어요 동기화
        syncCountsFromDbToRedis("comment", "like_count", "likes::comment::%s");
        syncCountsFromDbToRedis("comment", "dislike_count", "dislikes::comment::%s");

        log.info("[Sync] DB -> Redis :: 반응(좋아요/싫어요) 수 동기화를 완료했습니다.");
    }

    @Transactional(readOnly = true)
    public void syncPostViewCountsToRedis() {
        log.info("[Sync] DB -> Redis :: 게시물 조회수 동기화를 시작합니다.");

        String sql = "SELECT id, view_count FROM post WHERE view_count > 0";
        String redisKeyFormat = "views::post::%s";

        jdbcTemplate.query(sql, rs -> {
            String postId = rs.getString("id");
            long viewCount = rs.getLong("view_count");
            redisTemplate.opsForValue().set(String.format(redisKeyFormat, postId), String.valueOf(viewCount));
        });

        log.info("[Sync] DB -> Redis :: 게시물 조회수 동기화를 완료했습니다.");
    }

    private void syncCountsFromDbToRedis(String targetTable, String dbColumn, String redisKeyFormat) {
        String targetType = targetTable.toUpperCase(); // 'POST' 또는 'COMMENT'
        String reactionType = dbColumn.contains("like") ? "LIKE" : "DISLIKE";

        String sql = """
                SELECT target_id, COUNT(*) AS count
                FROM reaction
                WHERE target_type = ? AND type = ?
                GROUP BY target_id
                """;

        jdbcTemplate.query(sql, ps -> {
            ps.setString(1, targetType);
            ps.setString(2, reactionType);
        }, rs -> {
            String targetId = rs.getString("target_id");
            long count = rs.getLong("count");

            redisTemplate.opsForValue().set(String.format(redisKeyFormat, targetId), String.valueOf(count));
        });
    }
}