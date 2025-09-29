package com.algangi.mongle.stats.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsSyncService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void syncCounts(String pattern, String tableName, String columnName) {
        List<Object[]> batchArgs = new ArrayList<>();

        try (Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(pattern).count(1000).build())
        ) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                String count = redisTemplate.opsForValue().get(key);

                if (count != null) {
                    try {
                        String[] parts = key.split("::");
                        if (parts.length > 1) {
                            String id = parts[parts.length - 1];
                            batchArgs.add(new Object[]{Long.parseLong(count), id});
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Redis 키 '{}'의 값이 숫자 형식이 아닙니다. 값: {}", key, count);
                    }
                }
            }
        }

        if (!batchArgs.isEmpty()) {
            String sql = String.format("UPDATE %s SET %s = ? WHERE id = ?", tableName, columnName);
            jdbcTemplate.batchUpdate(sql, batchArgs);
            log.debug("{} 테이블의 {} 컬럼에 {}건의 데이터를 동기화했습니다.", tableName, columnName, batchArgs.size());
        }
    }
}