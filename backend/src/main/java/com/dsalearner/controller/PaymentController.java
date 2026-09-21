package com.dsalearner.controller;

import com.dsalearner.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentService.OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(paymentService.createProOrder(userId));
    }

    /** Called by the frontend after Razorpay checkout completes. */
    @PostMapping("/verify")
    public ResponseEntity<Void> verify(
            @RequestBody VerifyRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        paymentService.handlePaymentSuccess(req.orderId(), req.paymentId(), req.signature());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscription")
    public ResponseEntity<PaymentService.SubscriptionResponse> subscription(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(paymentService.getSubscription(userId));
    }

    public record VerifyRequest(String orderId, String paymentId, String signature) {}
}
