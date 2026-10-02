package com.dsalearner.subscription.service;

import com.dsalearner.subscription.dto.SubscriptionStatusDto;
import com.dsalearner.subscription.entity.LangSubscription;
import com.dsalearner.subscription.entity.PaymentEvent;
import com.dsalearner.subscription.entity.SubscriptionPlan;
import com.dsalearner.subscription.repository.LangSubscriptionRepository;
import com.dsalearner.subscription.repository.PaymentEventRepository;
import com.dsalearner.subscription.repository.SubscriptionPlanRepository;
import com.dsalearner.economy.analytics.EconomyEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock LangSubscriptionRepository subscriptionRepository;
    @Mock SubscriptionPlanRepository planRepository;
    @Mock PaymentEventRepository paymentEventRepository;
    @Mock EconomyEventService economyEventService;

    @InjectMocks SubscriptionService subscriptionService;

    private final UUID userId = UUID.randomUUID();

    private SubscriptionPlan proMonthlyPlan() {
        return SubscriptionPlan.builder()
                .planCode("PRO_MONTHLY")
                .displayName("Pro Monthly")
                .pricePaise(49900)
                .currency("INR")
                .intervalDays(30)
                .isActive(true)
                .build();
    }

    @Test
    void activateSubscription_setsProAndExpiry() {
        when(planRepository.findByPlanCodeAndIsActiveTrue("PRO_MONTHLY"))
                .thenReturn(Optional.of(proMonthlyPlan()));
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        LangSubscription saved = LangSubscription.builder()
                .userId(userId).planCode("PRO_MONTHLY").status(LangSubscription.Status.ACTIVE)
                .currentPeriodStart(Instant.now())
                .currentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(subscriptionRepository.save(any())).thenReturn(saved);

        SubscriptionStatusDto result = subscriptionService.activateSubscription(userId, "token123", "order123", "PRO_MONTHLY");

        assertTrue(result.isPro());
        assertEquals("PRO_MONTHLY", result.planCode());
        verify(subscriptionRepository).save(any(LangSubscription.class));
    }

    @Test
    void handleWebhookEvent_duplicateOrderId_isIdempotent() {
        when(paymentEventRepository.existsByPlayOrderIdAndStatus("order123", PaymentEvent.EventStatus.PROCESSED))
                .thenReturn(true);

        subscriptionService.handleWebhookEvent("payment.captured", "order123", "token", userId, "PRO_MONTHLY", "{}");

        verify(paymentEventRepository, never()).save(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void handleWebhookEvent_capturedEvent_activatesSubscription() {
        when(paymentEventRepository.existsByPlayOrderIdAndStatus("order123", PaymentEvent.EventStatus.PROCESSED))
                .thenReturn(false);
        PaymentEvent savedEvent = PaymentEvent.builder().playOrderId("order123")
                .status(PaymentEvent.EventStatus.RECEIVED).build();
        when(paymentEventRepository.save(any())).thenReturn(savedEvent);
        when(planRepository.findByPlanCodeAndIsActiveTrue("PRO_MONTHLY"))
                .thenReturn(Optional.of(proMonthlyPlan()));
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        LangSubscription activated = LangSubscription.builder().userId(userId).planCode("PRO_MONTHLY")
                .status(LangSubscription.Status.ACTIVE)
                .currentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(subscriptionRepository.save(any())).thenReturn(activated);

        subscriptionService.handleWebhookEvent("payment.captured", "order123", "token", userId, "PRO_MONTHLY", "{}");

        verify(subscriptionRepository, atLeastOnce()).save(any(LangSubscription.class));
    }

    @Test
    void activateSubscription_sameToken_isIdempotent() {
        when(paymentEventRepository.existsByPlayPurchaseTokenAndStatus("token123", PaymentEvent.EventStatus.PROCESSED))
                .thenReturn(true);
        LangSubscription existing = LangSubscription.builder()
                .userId(userId).planCode("PRO_MONTHLY").status(LangSubscription.Status.ACTIVE)
                .currentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS)).build();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        SubscriptionStatusDto result = subscriptionService.activateSubscription(userId, "token123", "order123", "PRO_MONTHLY");

        assertEquals("PRO_MONTHLY", result.planCode());
        verify(subscriptionRepository, never()).save(any());
        verify(planRepository, never()).findByPlanCodeAndIsActiveTrue(any());
    }

    @Test
    void activateSubscription_tokenBoundToDifferentUser_throwsIllegalArgument() {
        UUID otherUserId = UUID.randomUUID();
        when(paymentEventRepository.existsByPlayPurchaseTokenAndStatus("tokenX", PaymentEvent.EventStatus.PROCESSED))
                .thenReturn(false);
        LangSubscription otherSub = LangSubscription.builder()
                .userId(otherUserId).planCode("PRO_MONTHLY").status(LangSubscription.Status.ACTIVE)
                .playPurchaseToken("tokenX").build();
        when(subscriptionRepository.findByPlayPurchaseToken("tokenX")).thenReturn(Optional.of(otherSub));

        assertThrows(IllegalArgumentException.class,
                () -> subscriptionService.activateSubscription(userId, "tokenX", "orderX", "PRO_MONTHLY"));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void restorePurchase_noSubscription_returnsFree() {
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());

        SubscriptionStatusDto result = subscriptionService.restorePurchase(userId);

        assertEquals("FREE", result.planCode());
        assertFalse(result.isPro());
    }

    @Test
    void restorePurchase_activePro_returnsProStatus() {
        LangSubscription sub = LangSubscription.builder()
                .userId(userId).planCode("PRO_MONTHLY").status(LangSubscription.Status.ACTIVE)
                .currentPeriodEnd(Instant.now().plus(15, ChronoUnit.DAYS)).build();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(sub));

        SubscriptionStatusDto result = subscriptionService.restorePurchase(userId);

        assertEquals("PRO_MONTHLY", result.planCode());
        assertTrue(result.isPro());
    }

    @Test
    void handleWebhookEvent_cancelledEvent_setsStatusCancelled() {
        when(paymentEventRepository.existsByPlayOrderIdAndStatus("order123", PaymentEvent.EventStatus.PROCESSED))
                .thenReturn(false);
        PaymentEvent savedEvent = PaymentEvent.builder().playOrderId("order123")
                .status(PaymentEvent.EventStatus.RECEIVED).build();
        when(paymentEventRepository.save(any())).thenReturn(savedEvent);
        LangSubscription existing = LangSubscription.builder().userId(userId).planCode("PRO_MONTHLY")
                .status(LangSubscription.Status.ACTIVE).build();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any())).thenReturn(existing);

        subscriptionService.handleWebhookEvent("subscription.cancelled", "order123", null, userId, "PRO_MONTHLY", "{}");

        assertEquals(LangSubscription.Status.CANCELLED, existing.getStatus());
        assertNotNull(existing.getCancelledAt());
    }
}
