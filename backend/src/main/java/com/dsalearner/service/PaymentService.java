package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    // PRO plan: ₹499/month → 500 paid credits per billing cycle
    public static final int PRO_PRICE_PAISE = 49900;
    public static final int PRO_CREDITS     = 500;
    public static final String PLAN_PRO     = "PRO";
    public static final String PLAN_FREE    = "FREE";

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final PaymentOrderRepository orderRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final CreditService creditService;
    private final UserRepository userRepository;

    /** Create a Razorpay order for PRO upgrade. */
    @Transactional
    public OrderResponse createProOrder(UUID userId) {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", PRO_PRICE_PAISE);
            options.put("currency", "INR");
            options.put("receipt", "pro_" + userId.toString().substring(0, 8));
            options.put("payment_capture", 1);

            Order rzpOrder = client.orders.create(options);
            String rzpOrderId = rzpOrder.get("id");

            User user = userRepository.getReferenceById(userId);
            PaymentOrder po = PaymentOrder.builder()
                    .user(user)
                    .razorpayOrderId(rzpOrderId)
                    .amountPaise(PRO_PRICE_PAISE)
                    .creditsToAdd(PRO_CREDITS)
                    .planToUpgrade(PLAN_PRO)
                    .build();
            orderRepository.save(po);

            return new OrderResponse(rzpOrderId, PRO_PRICE_PAISE, "INR", keyId);
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    /** Verify Razorpay payment signature and fulfil the order. */
    @Transactional
    public void handlePaymentSuccess(String orderId, String paymentId, String signature) {
        // Verify signature: HMAC-SHA256(orderId + "|" + paymentId, keySecret)
        try {
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_order_id", orderId);
            attrs.put("razorpay_payment_id", paymentId);
            attrs.put("razorpay_signature", signature);
            Utils.verifyPaymentSignature(attrs, keySecret);
        } catch (RazorpayException e) {
            throw new IllegalArgumentException("Payment signature verification failed");
        }

        PaymentOrder po = orderRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown order: " + orderId));

        if (po.getStatus() == PaymentOrder.Status.PAID) return; // idempotent

        po.setRazorpayPaymentId(paymentId);
        po.setStatus(PaymentOrder.Status.PAID);
        po.setUpdatedAt(Instant.now());
        orderRepository.save(po);

        UUID userId = po.getUser().getId();

        // Add paid credits to wallet
        if (po.getCreditsToAdd() > 0) {
            creditService.addPaidCredits(userId, po.getCreditsToAdd(), "PRO plan purchase");
        }

        // Upgrade subscription to PRO
        if (PLAN_PRO.equals(po.getPlanToUpgrade())) {
            UserSubscription sub = subscriptionRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        User user = userRepository.getReferenceById(userId);
                        return UserSubscription.builder().user(user).build();
                    });
            sub.setPlan(UserSubscription.Plan.PRO);
            sub.setStatus(UserSubscription.Status.ACTIVE);
            sub.setCurrentPeriodStart(Instant.now());
            sub.setCurrentPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
            sub.setUpdatedAt(Instant.now());
            subscriptionRepository.save(sub);
        }
    }

    public SubscriptionResponse getSubscription(UUID userId) {
        UserSubscription sub = subscriptionRepository.findByUserId(userId)
                .orElse(null);
        String plan = sub != null ? sub.getPlan().name() : "FREE";
        boolean isPro = sub != null && sub.isPro();
        Instant expiresAt = sub != null ? sub.getCurrentPeriodEnd() : null;
        return new SubscriptionResponse(plan, isPro, expiresAt);
    }

    public record OrderResponse(String orderId, int amountPaise, String currency, String keyId) {}
    public record SubscriptionResponse(String plan, boolean isPro, Instant expiresAt) {}
}
