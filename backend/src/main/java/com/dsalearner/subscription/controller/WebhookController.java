package com.dsalearner.subscription.controller;

import com.dsalearner.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        if (signature == null || signature.isBlank()) {
            log.warn("Razorpay webhook received without signature header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Utils.verifyWebhookSignature(rawBody, signature, webhookSecret);
        } catch (RazorpayException e) {
            log.warn("Razorpay webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            JsonNode payload = objectMapper.readTree(rawBody);
            String eventType = payload.path("event").asText();

            String orderId = null;
            String paymentId = null;
            UUID userId = null;
            String planCode = null;

            JsonNode entity = payload.path("payload").path("payment").path("entity");
            if (!entity.isMissingNode()) {
                paymentId = entity.path("id").asText(null);
                orderId = entity.path("order_id").asText(null);
                JsonNode notes = entity.path("notes");
                if (!notes.isMissingNode()) {
                    String rawUserId = notes.path("user_id").asText(null);
                    if (rawUserId != null) {
                        try { userId = UUID.fromString(rawUserId); } catch (IllegalArgumentException ignored) {}
                    }
                    planCode = notes.path("plan_code").asText(null);
                }
            }

            if (userId != null && planCode != null && orderId != null) {
                subscriptionService.handleWebhookEvent(eventType, orderId, paymentId, userId, planCode, rawBody);
            } else {
                log.warn("Webhook event {} missing required fields (userId={}, planCode={}, orderId={})",
                        eventType, userId, planCode, orderId);
            }
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }
}
