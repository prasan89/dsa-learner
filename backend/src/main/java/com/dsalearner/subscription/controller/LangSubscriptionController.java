package com.dsalearner.subscription.controller;

import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.subscription.dto.SubscriptionPlanDto;
import com.dsalearner.subscription.dto.SubscriptionStatusDto;
import com.dsalearner.subscription.service.EntitlementService;
import com.dsalearner.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class LangSubscriptionController {

    private final EntitlementService entitlementService;
    private final SubscriptionService subscriptionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/me")
    public ResponseEntity<SubscriptionStatusDto> getMySubscription(Authentication authentication) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(entitlementService.getStatus(userId));
    }

    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanDto>> getPlans() {
        return ResponseEntity.ok(subscriptionService.getActivePlans());
    }

    @PostMapping("/order")
    public ResponseEntity<SubscriptionService.CreateOrderResponse> createOrder(
            Authentication authentication,
            @RequestBody CreateOrderRequest request) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(subscriptionService.createOrder(userId, request.planCode()));
    }

    @PostMapping("/verify")
    public ResponseEntity<SubscriptionStatusDto> verify(
            Authentication authentication,
            @RequestBody VerifyPaymentRequest request) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(subscriptionService.verifyAndActivate(
                userId, request.orderId(), request.paymentId(), request.signature(), request.planCode()));
    }

    @PostMapping("/verify-play")
    public ResponseEntity<SubscriptionStatusDto> verifyPlay(
            Authentication authentication,
            @RequestBody VerifyPlayRequest request) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(subscriptionService.activateSubscription(
                userId, request.purchaseToken(), request.orderId(), request.planCode()));
    }

    record CreateOrderRequest(String planCode) {}
    record VerifyPaymentRequest(String orderId, String paymentId, String signature, String planCode) {}
    record VerifyPlayRequest(String planCode, String purchaseToken, String orderId) {}
}
