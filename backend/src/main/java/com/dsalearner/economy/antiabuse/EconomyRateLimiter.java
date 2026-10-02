package com.dsalearner.economy.antiabuse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Per-user rate limits on economy-mutating endpoints.
 * Backed by Redis; fails open (allows request) when Redis is unavailable.
 *
 * Limits (per user per window):
 *   lesson completion  : 60 / hour   (≈1/min, generous for streaks)
 *   resource collection: 20 / 5 min  (prevents rapid-fire collect spam)
 *   building purchase  : 10 / 10 min
 *   building upgrade   : 10 / 10 min
 *   subscription verify: 5  / hour
 */
@Service
@Slf4j
public class EconomyRateLimiter {

    private static final String PREFIX = "eco_rl:";

    record Limit(int max, Duration window) {}

    private static final Limit LESSON     = new Limit(60, Duration.ofHours(1));
    private static final Limit COLLECT    = new Limit(20, Duration.ofMinutes(5));
    private static final Limit BUILD      = new Limit(10, Duration.ofMinutes(10));
    private static final Limit UPGRADE    = new Limit(10, Duration.ofMinutes(10));
    private static final Limit SUBSCRIBE  = new Limit(5,  Duration.ofHours(1));

    private final Optional<StringRedisTemplate> redis;

    @Autowired
    public EconomyRateLimiter(@Autowired(required = false) StringRedisTemplate redis) {
        this.redis = Optional.ofNullable(redis);
    }

    public boolean isLessonAllowed(UUID userId)   { return check(userId, "lesson",    LESSON); }
    public boolean isCollectAllowed(UUID userId)  { return check(userId, "collect",   COLLECT); }
    public boolean isBuildAllowed(UUID userId)    { return check(userId, "build",     BUILD); }
    public boolean isUpgradeAllowed(UUID userId)  { return check(userId, "upgrade",   UPGRADE); }
    public boolean isSubscribeAllowed(UUID userId){ return check(userId, "subscribe", SUBSCRIBE); }

    private boolean check(UUID userId, String action, Limit limit) {
        if (redis.isEmpty()) return true;
        String key = PREFIX + action + ":" + userId;
        try {
            Long count = redis.get().opsForValue().increment(key);
            if (count != null && count == 1) {
                redis.get().expire(key, limit.window());
            }
            return count == null || count <= limit.max();
        } catch (Exception e) {
            log.warn("EconomyRateLimiter Redis error (fails open): action={} userId={} err={}", action, userId, e.getMessage());
            return true;
        }
    }
}
