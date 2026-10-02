package com.dsalearner.subscription.service;

import com.dsalearner.subscription.dto.SubscriptionStatusDto;
import com.dsalearner.subscription.entity.LangSubscription;
import com.dsalearner.subscription.repository.LangSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitlementServiceTest {

    @Mock
    LangSubscriptionRepository subscriptionRepository;

    @InjectMocks
    EntitlementService entitlementService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void isPro_whenNoSubscription_returnsFalse() {
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertFalse(entitlementService.isPro(userId));
    }

    @Test
    void isPro_whenFreeSubscription_returnsFalse() {
        LangSubscription sub = LangSubscription.builder()
                .userId(userId).planCode("FREE").status(LangSubscription.Status.ACTIVE).build();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(sub));
        assertFalse(entitlementService.isPro(userId));
    }

    @Test
    void isPro_whenProActive_returnsTrue() {
        LangSubscription sub = LangSubscription.builder()
                .userId(userId).planCode("PRO_MONTHLY").status(LangSubscription.Status.ACTIVE)
                .currentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(sub));
        assertTrue(entitlementService.isPro(userId));
    }

    @Test
    void isPro_whenProExpired_returnsFalse() {
        LangSubscription sub = LangSubscription.builder()
                .userId(userId).planCode("PRO_MONTHLY").status(LangSubscription.Status.ACTIVE)
                .currentPeriodEnd(Instant.now().minus(1, ChronoUnit.DAYS)).build();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(sub));
        assertFalse(entitlementService.isPro(userId));
    }

    @Test
    void getStatus_whenNoSubscription_returnsFreeStatus() {
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        SubscriptionStatusDto status = entitlementService.getStatus(userId);
        assertEquals("FREE", status.planCode());
        assertFalse(status.isPro());
    }
}
