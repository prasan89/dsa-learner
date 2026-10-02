package com.dsalearner.civilization.controller;

import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.civilization.dto.CoinPackageDto;
import com.dsalearner.civilization.service.CoinPurchaseService;
import com.dsalearner.security.DomainAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/v1/civilization/coin-packages")
@RequiredArgsConstructor
public class CoinPurchaseController {

    private final CoinPurchaseService coinPurchaseService;
    private final CurrentUserProvider currentUserProvider;
    private final DomainAuthorizationService domainAuthService;

    @GetMapping
    public ResponseEntity<List<CoinPackageDto>> listPackages() {
        return ResponseEntity.ok(coinPurchaseService.getPackages());
    }

    @PostMapping("/{packageCode}/order")
    public ResponseEntity<CoinPurchaseService.CreateCoinOrderResponse> createOrder(
            @PathVariable String packageCode,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(coinPurchaseService.createOrder(userId, packageCode));
    }

    @PostMapping("/{packageCode}/verify")
    public ResponseEntity<CoinPurchaseService.CoinCreditResponse> verifyRazorpay(
            @PathVariable String packageCode,
            @RequestBody VerifyRazorpayCoinRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(coinPurchaseService.verifyAndCredit(
                userId, request.orderId(), request.paymentId(),
                request.signature(), packageCode, request.languageCode()));
    }

    @PostMapping("/verify-play")
    public ResponseEntity<CoinPurchaseService.CoinCreditResponse> verifyPlay(
            @RequestBody VerifyPlayCoinRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(coinPurchaseService.verifyPlayAndCredit(
                userId, request.purchaseToken(), request.orderId(),
                request.packageCode(), request.languageCode()));
    }

    public record VerifyRazorpayCoinRequest(String orderId, String paymentId, String signature, String languageCode) {}

    public record VerifyPlayCoinRequest(String packageCode, String purchaseToken, String orderId, String languageCode) {}
}
