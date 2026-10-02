package com.dsalearner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RateLimitService {

    private static final int AI_REVIEW_DAILY_LIMIT = 5;
    private final Optional<StringRedisTemplate> redisTemplate;

    @Autowired
    public RateLimitService(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = Optional.ofNullable(redisTemplate);
    }

    public boolean allowAiReview(UUID userId) {
        if (redisTemplate.isEmpty()) return true;
        String key = "ai:review:" + userId + ":" + java.time.LocalDate.now();
        Long count = redisTemplate.get().opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.get().expire(key, Duration.ofHours(25));
        }
        return count <= AI_REVIEW_DAILY_LIMIT;
    }

    public int remainingAiReviews(UUID userId) {
        if (redisTemplate.isEmpty()) return AI_REVIEW_DAILY_LIMIT;
        String key = "ai:review:" + userId + ":" + java.time.LocalDate.now();
        String val = redisTemplate.get().opsForValue().get(key);
        int used = val == null ? 0 : Integer.parseInt(val);
        return Math.max(0, AI_REVIEW_DAILY_LIMIT - used);
    }
}
