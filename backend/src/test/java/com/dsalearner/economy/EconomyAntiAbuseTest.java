package com.dsalearner.economy;

import com.dsalearner.economy.analytics.EconomyEventService;
import com.dsalearner.economy.analytics.LangoaEconomyEvent;
import com.dsalearner.economy.analytics.LangoaEconomyEventRepository;
import com.dsalearner.economy.antiabuse.EconomyRateLimiter;
import com.dsalearner.economy.antiabuse.LangoaSuspiciousActivity;
import com.dsalearner.economy.antiabuse.LangoaSuspiciousActivityRepository;
import com.dsalearner.economy.antiabuse.SuspiciousActivityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EconomyAntiAbuseTest {

    // ── EconomyRateLimiter ────────────────────────────────────────────────────

    @Test
    void rateLimiter_noRedis_allowsAll() {
        EconomyRateLimiter limiter = new EconomyRateLimiter(null);
        UUID userId = UUID.randomUUID();
        assertTrue(limiter.isLessonAllowed(userId));
        assertTrue(limiter.isCollectAllowed(userId));
        assertTrue(limiter.isBuildAllowed(userId));
        assertTrue(limiter.isUpgradeAllowed(userId));
        assertTrue(limiter.isSubscribeAllowed(userId));
    }

    @Test
    void rateLimiter_belowLimit_allowed() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenReturn(1L);

        EconomyRateLimiter limiter = new EconomyRateLimiter(redis);
        UUID userId = UUID.randomUUID();
        assertTrue(limiter.isLessonAllowed(userId));
        verify(redis).expire(anyString(), any(Duration.class));
    }

    @Test
    void rateLimiter_atLimit_denied() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        // Collect limit is 20; return 21
        when(ops.increment(anyString())).thenReturn(21L);

        EconomyRateLimiter limiter = new EconomyRateLimiter(redis);
        assertFalse(limiter.isCollectAllowed(UUID.randomUUID()));
    }

    @Test
    void rateLimiter_redisException_failsOpen() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenThrow(new RuntimeException("Redis down"));

        EconomyRateLimiter limiter = new EconomyRateLimiter(redis);
        assertTrue(limiter.isBuildAllowed(UUID.randomUUID()), "Should fail open on Redis error");
    }

    // ── SuspiciousActivityService ─────────────────────────────────────────────

    @Mock
    LangoaSuspiciousActivityRepository suspiciousRepo;

    @InjectMocks
    SuspiciousActivityService suspiciousActivityService;

    @Test
    void flag_savesRecord() {
        UUID userId = UUID.randomUUID();
        suspiciousActivityService.flag(userId, "LESSON_RATE_LIMIT", "too many lessons");

        ArgumentCaptor<LangoaSuspiciousActivity> captor =
                ArgumentCaptor.forClass(LangoaSuspiciousActivity.class);
        verify(suspiciousRepo).save(captor.capture());

        LangoaSuspiciousActivity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals("LESSON_RATE_LIMIT", saved.getReason());
        assertEquals("too many lessons", saved.getDetail());
    }

    @Test
    void flag_repoException_doesNotPropagate() {
        when(suspiciousRepo.save(any())).thenThrow(new RuntimeException("DB error"));
        // Must not throw
        assertDoesNotThrow(() ->
                suspiciousActivityService.flag(UUID.randomUUID(), "TEST", "detail"));
    }

    // ── EconomyEventService ───────────────────────────────────────────────────

    @Mock
    LangoaEconomyEventRepository economyEventRepo;

    @InjectMocks
    EconomyEventService economyEventService;

    @Test
    void record_savesEvent() {
        UUID userId = UUID.randomUUID();
        economyEventService.record("lesson_reward_granted", userId, "de", 50L, "COINS", "lesson-abc");

        ArgumentCaptor<LangoaEconomyEvent> captor = ArgumentCaptor.forClass(LangoaEconomyEvent.class);
        verify(economyEventRepo).save(captor.capture());

        LangoaEconomyEvent event = captor.getValue();
        assertEquals("lesson_reward_granted", event.getEventName());
        assertEquals(userId, event.getUserId());
        assertEquals(50L, event.getAmount());
        assertEquals("COINS", event.getCurrencyType());
    }

    @Test
    void record_repoException_doesNotPropagate() {
        when(economyEventRepo.save(any())).thenThrow(new RuntimeException("DB error"));
        assertDoesNotThrow(() ->
                economyEventService.record("test_event", UUID.randomUUID()));
    }
}
