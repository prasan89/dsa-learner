package com.dsalearner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS    = 5;
    private static final Duration WINDOW     = Duration.ofMinutes(15);
    private static final String PREFIX       = "login_attempts:";

    private final StringRedisTemplate redis;

    /** Returns true if this key is NOT rate-limited (request is allowed). */
    public boolean isAllowed(String key) {
        String redisKey = PREFIX + key;
        Long attempts = redis.opsForValue().increment(redisKey);
        if (attempts != null && attempts == 1) {
            redis.expire(redisKey, WINDOW);
        }
        return attempts == null || attempts <= MAX_ATTEMPTS;
    }

    public void resetAttempts(String key) {
        redis.delete(PREFIX + key);
    }
}
