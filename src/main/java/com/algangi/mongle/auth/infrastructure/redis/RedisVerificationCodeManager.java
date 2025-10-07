package com.algangi.mongle.auth.infrastructure.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.algangi.mongle.auth.application.service.VerificationCodeManager;

import java.time.Duration;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisVerificationCodeManager implements VerificationCodeManager {

    private static final String KEY_PREFIX = "AUTH_CODE:";
    private static final Duration TTL = Duration.ofMinutes(10);
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String email, String code) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, code, TTL);
    }

    @Override
    public String getCode(String email) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + email);
    }

    @Override
    public void deleteCode(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
