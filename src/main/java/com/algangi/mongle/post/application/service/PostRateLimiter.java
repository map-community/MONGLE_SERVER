package com.algangi.mongle.post.application.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.exception.PostErrorCode;

import java.time.Duration;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostRateLimiter {

    private static final String RATE_LIMIT_KEY_PREFIX = "post-creation:rate-limit:";
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(3);
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlocked(String userId) {
        String key = RATE_LIMIT_KEY_PREFIX + userId;
        Boolean isSuccess = redisTemplate.opsForValue()
            .setIfAbsent(key, "blocked", BLOCK_DURATION);
        return isSuccess == null || !isSuccess;
    }

    public void checkRateLimit(String userId) {
        if (isBlocked(userId)) {
            throw new ApplicationException(PostErrorCode.POST_RATE_LIMIT_EXCEEDED);
        }
    }
}
