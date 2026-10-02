package com.dsalearner.subscription.service;

import com.dsalearner.economy.analytics.EconomyEventService;
import com.dsalearner.subscription.dto.SubscriptionPlanDto;
import com.dsalearner.subscription.dto.SubscriptionStatusDto;
import com.dsalearner.subscription.entity.LangSubscription;
import com.dsalearner.subscription.entity.PaymentEvent;
import com.dsalearner.subscription.entity.SubscriptionPlan;
import com.dsalearner.subscription.repository.LangSubscriptionRepository;
import com.dsalearner.subscription.repository.PaymentEventRepository;
import com.dsalearner.subscription.repository.SubscriptionPlanRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final LangSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final EconomyEventService economyEventService;

    public CreateOrderResponse createOrder(UUID userId, String planCode) {
        SubscriptionPlan plan = planRepository.findByPlanCodeAndIsActiveTrue(planCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown plan: " + planCode));

        if (plan.getPricePaise() == 0) {
            throw new IllegalArgumentException("Cannot create order for free plan");
        }

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", plan.getPricePaise());
            options.put("currency", plan.getCurrency());
            options.put("receipt", "lang_" + userId.toString().substring(0, 8));
            options.put("payment_capture", 1);
            JSONObject notes = new JSONObject();
            notes.put("user_id", userId.toString());
            notes.put("plan_code", planCode);
            options.put("notes", notes);

            Order rzpOrder = client.orders.create(options);
            String rzpOrderId = rzpOrder.get("id");

            PaymentEvent event = PaymentEvent.builder()
                    .userId(userId)
                    .playOrderId(rzpOrderId)
                    .eventType("ORDER_CREATED")
                    .planCode(planCode)
                    .status(PaymentEvent.EventStatus.RECEIVED)
                    .build();
            paymentEventRepository.save(event);

            return new CreateOrderResponse(rzpOrderId, plan.getPricePaise(), plan.getCurrency(), keyId, planCode);
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    public SubscriptionStatusDto verifyAndActivate(UUID userId, String orderId, String paymentId,
                                                    String signature, String planCode) {
        try {
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_order_id", orderId);
            attrs.put("razorpay_payment_id", paymentId);
            attrs.put("razorpay_signature", signature);
            Utils.verifyPaymentSignature(attrs, keySecret);
        } catch (RazorpayException e) {
            throw new IllegalArgumentException("Payment signature verification failed");
        }
        return activateSubscription(userId, paymentId, orderId, planCode);
    }

    public SubscriptionStatusDto activateSubscription(UUID userId, String purchaseToken,
                                                       String orderId, String planCode) {
        // Idempotency: token already fully processed for this user → return current status
        if (purchaseToken != null && !purchaseToken.isBlank()) {
            if (paymentEventRepository.existsByPlayPurchaseTokenAndStatus(purchaseToken, PaymentEvent.EventStatus.PROCESSED)) {
                return subscriptionRepository.findByUserId(userId)
                        .map(s -> new SubscriptionStatusDto(s.getPlanCode(), s.isProActive(),
                                s.getCurrentPeriodStart(), s.getCurrentPeriodEnd()))
                        .orElseThrow(() -> new IllegalStateException("Processed event but no subscription found for userId=" + userId));
            }

            // Token-to-user binding: reject if token belongs to a different user
            subscriptionRepository.findByPlayPurchaseToken(purchaseToken)
                    .filter(existing -> !existing.getUserId().equals(userId))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Purchase token is bound to a different account");
                    });
        }

        SubscriptionPlan plan = planRepository.findByPlanCodeAndIsActiveTrue(planCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown plan: " + planCode));

        LangSubscription sub = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> LangSubscription.builder().userId(userId).build());

        Instant now = Instant.now();
        sub.setPlanCode(planCode);
        sub.setStatus(LangSubscription.Status.ACTIVE);
        sub.setPlayPurchaseToken(purchaseToken);
        sub.setPlayOrderId(orderId);
        sub.setCurrentPeriodStart(now);
        sub.setCurrentPeriodEnd(plan.getIntervalDays() > 0
                ? now.plus(plan.getIntervalDays(), ChronoUnit.DAYS)
                : null);
        sub.setCancelledAt(null);
        subscriptionRepository.save(sub);

        if (purchaseToken != null && !purchaseToken.isBlank()) {
            PaymentEvent processed = PaymentEvent.builder()
                    .userId(userId)
                    .playPurchaseToken(purchaseToken)
                    .playOrderId(orderId)
                    .eventType("PLAY_SUBSCRIPTION_VERIFIED")
                    .planCode(planCode)
                    .status(PaymentEvent.EventStatus.PROCESSED)
                    .processedAt(now)
                    .build();
            paymentEventRepository.save(processed);
        }

        log.info("Subscription activated for userId={} plan={}", userId, planCode);
        economyEventService.record("SUBSCRIPTION_STARTED", userId, null, planCode);
        return new SubscriptionStatusDto(planCode, sub.isProActive(),
                sub.getCurrentPeriodStart(), sub.getCurrentPeriodEnd());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public SubscriptionStatusDto restorePurchase(UUID userId) {
        LangSubscription sub = subscriptionRepository.findByUserId(userId)
                .orElse(null);
        if (sub == null) {
            return new SubscriptionStatusDto("FREE", false, null, null);
        }
        return new SubscriptionStatusDto(sub.getPlanCode(), sub.isProActive(),
                sub.getCurrentPeriodStart(), sub.getCurrentPeriodEnd());
    }

    public void handleWebhookEvent(String eventType, String orderId, String purchaseToken,
                                    UUID userId, String planCode, String rawPayload) {
        if (paymentEventRepository.existsByPlayOrderIdAndStatus(orderId, PaymentEvent.EventStatus.PROCESSED)) {
            log.info("Duplicate webhook event for orderId={}, skipping", orderId);
            return;
        }

        PaymentEvent event = PaymentEvent.builder()
                .userId(userId)
                .playOrderId(orderId)
                .playPurchaseToken(purchaseToken)
                .eventType(eventType)
                .planCode(planCode)
                .status(PaymentEvent.EventStatus.RECEIVED)
                .rawPayload(rawPayload)
                .build();
        event = paymentEventRepository.save(event);

        try {
            switch (eventType) {
                case "payment.captured", "subscription.charged" ->
                        activateSubscription(userId, purchaseToken, orderId, planCode);
                case "subscription.cancelled" -> {
                    subscriptionRepository.findByUserId(userId).ifPresent(sub -> {
                        sub.setStatus(LangSubscription.Status.CANCELLED);
                        sub.setCancelledAt(Instant.now());
                        subscriptionRepository.save(sub);
                    });
                }
                case "payment.failed" -> {
                    subscriptionRepository.findByUserId(userId).ifPresent(sub -> {
                        sub.setStatus(LangSubscription.Status.PAST_DUE);
                        subscriptionRepository.save(sub);
                    });
                }
                default -> log.warn("Unhandled webhook eventType={}", eventType);
            }
            event.setStatus(PaymentEvent.EventStatus.PROCESSED);
            event.setProcessedAt(Instant.now());
        } catch (Exception e) {
            log.error("Failed processing webhook event orderId={}: {}", orderId, e.getMessage(), e);
            event.setStatus(PaymentEvent.EventStatus.FAILED);
            event.setFailureReason(e.getMessage());
        }
        paymentEventRepository.save(event);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<SubscriptionPlanDto> getActivePlans() {
        return planRepository.findByIsActiveTrue().stream()
                .map(p -> new SubscriptionPlanDto(
                        p.getPlanCode(), p.getDisplayName(), p.getPricePaise(),
                        p.getCurrency(), p.getIntervalDays(), p.getPlayProductId()))
                .collect(Collectors.toList());
    }

    public record CreateOrderResponse(String orderId, int amountPaise, String currency,
                                       String razorpayKeyId, String planCode) {}
}
