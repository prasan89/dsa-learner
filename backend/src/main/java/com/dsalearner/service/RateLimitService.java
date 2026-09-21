package com.dsalearner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final int AI_REVIEW_DAILY_LIMIT = 5;
    private final StringRedisTemplate redisTemplate;

    public boolean allowAiReview(UUID userId) {
        String key = "ai:review:" + userId + ":" + java.time.LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofHours(25));
        }
        return count <= AI_REVIEW_DAILY_LIMIT;
    }

    public int remainingAiReviews(UUID userId) {
        String key = "ai:review:" + userId + ":" + java.time.LocalDate.now();
        String val = redisTemplate.opsForValue().get(key);
        int used = val == null ? 0 : Integer.parseInt(val);
        return Math.max(0, AI_REVIEW_DAILY_LIMIT - used);
    }
}
