package com.dsalearner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS    = 5;
    private static final Duration WINDOW     = Duration.ofMinutes(15);
    private static final String PREFIX       = "login_attempts:";

    private final Optional<StringRedisTemplate> redis;

    @Autowired
    public LoginRateLimiter(@Autowired(required = false) StringRedisTemplate redis) {
        this.redis = Optional.ofNullable(redis);
    }

    /** Returns true if this key is NOT rate-limited (request is allowed). */
    public boolean isAllowed(String key) {
        if (redis.isEmpty()) return true;
        String redisKey = PREFIX + key;
        Long attempts = redis.get().opsForValue().increment(redisKey);
        if (attempts != null && attempts == 1) {
            redis.get().expire(redisKey, WINDOW);
        }
        return attempts == null || attempts <= MAX_ATTEMPTS;
    }

    public void resetAttempts(String key) {
        redis.ifPresent(r -> r.delete(PREFIX + key));
    }
}
