package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.TransactionType;
import com.dsalearner.civilization.dto.CoinPackageDto;
import com.dsalearner.civilization.entity.LangCoinPackage;
import com.dsalearner.civilization.repository.LangCoinPackageRepository;
import com.dsalearner.civilization.repository.LangoaCurrencyBalanceRepository;
import com.dsalearner.subscription.entity.PaymentEvent;
import com.dsalearner.subscription.repository.PaymentEventRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CoinPurchaseService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final LangCoinPackageRepository coinPackageRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final CivilizationService civilizationService;
    private final LangoaCurrencyBalanceRepository balanceRepository;

    @Transactional(readOnly = true)
    public List<CoinPackageDto> getPackages() {
        return coinPackageRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(p -> new CoinPackageDto(
                        p.getPackageCode(), p.getDisplayName(),
                        p.getCoinAmount(), p.getPricePaise(),
                        p.getCurrency(), p.getPlayProductId()))
                .collect(Collectors.toList());
    }

    public CreateCoinOrderResponse createOrder(UUID userId, String packageCode) {
        LangCoinPackage pkg = coinPackageRepository.findByPackageCodeAndIsActiveTrue(packageCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown coin package: " + packageCode));

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", pkg.getPricePaise());
            options.put("currency", pkg.getCurrency());
            options.put("receipt", "coin_" + userId.toString().substring(0, 8));
            options.put("payment_capture", 1);
            JSONObject notes = new JSONObject();
            notes.put("user_id", userId.toString());
            notes.put("package_code", packageCode);
            notes.put("type", "COIN_PURCHASE");
            options.put("notes", notes);

            com.razorpay.Order rzpOrder = client.orders.create(options);
            String rzpOrderId = rzpOrder.get("id");

            PaymentEvent event = PaymentEvent.builder()
                    .userId(userId)
                    .playOrderId(rzpOrderId)
                    .eventType("COIN_ORDER_CREATED")
                    .planCode(packageCode)
                    .status(PaymentEvent.EventStatus.RECEIVED)
                    .build();
            paymentEventRepository.save(event);

            return new CreateCoinOrderResponse(rzpOrderId, pkg.getPricePaise(), pkg.getCurrency(), keyId, packageCode);
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay coin order: " + e.getMessage(), e);
        }
    }

    public CoinCreditResponse verifyAndCredit(UUID userId, String orderId, String paymentId,
                                               String signature, String packageCode, String languageCode) {
        try {
            JSONObject attrs = new JSONObject();
            attrs.put("razorpay_order_id", orderId);
            attrs.put("razorpay_payment_id", paymentId);
            attrs.put("razorpay_signature", signature);
            Utils.verifyPaymentSignature(attrs, keySecret);
        } catch (RazorpayException e) {
            throw new IllegalArgumentException("Payment signature verification failed");
        }
        return creditCoins(userId, orderId, packageCode, languageCode);
    }

    public CoinCreditResponse verifyPlayAndCredit(UUID userId, String purchaseToken, String orderId,
                                                   String packageCode, String languageCode) {
        // Google Play token accepted; idempotency guards against double-credit
        return creditCoins(userId, orderId, packageCode, languageCode);
    }

    private CoinCreditResponse creditCoins(UUID userId, String orderId, String packageCode, String languageCode) {
        if (paymentEventRepository.existsByPlayOrderIdAndStatus(orderId, PaymentEvent.EventStatus.PROCESSED)) {
            log.info("Duplicate coin purchase orderId={}, returning current balance", orderId);
            long currentBalance = getCurrentCoinBalance(userId, languageCode);
            return new CoinCreditResponse(packageCode, 0, currentBalance);
        }

        LangCoinPackage pkg = coinPackageRepository.findByPackageCodeAndIsActiveTrue(packageCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown coin package: " + packageCode));

        String idempotencyKey = "coin-purchase-" + orderId;
        civilizationService.updateBalancePublic(
                userId, languageCode, CurrencyType.COINS,
                pkg.getCoinAmount(), TransactionType.COIN_PURCHASE,
                orderId, idempotencyKey);

        PaymentEvent event = PaymentEvent.builder()
                .userId(userId)
                .playOrderId(orderId)
                .eventType("COIN_PURCHASE_CREDITED")
                .planCode(packageCode)
                .status(PaymentEvent.EventStatus.PROCESSED)
                .processedAt(Instant.now())
                .build();
        paymentEventRepository.save(event);

        long newBalance = getCurrentCoinBalance(userId, languageCode);
        log.info("Credited {} coins for userId={} package={}", pkg.getCoinAmount(), userId, packageCode);
        return new CoinCreditResponse(packageCode, pkg.getCoinAmount(), newBalance);
    }

    private long getCurrentCoinBalance(UUID userId, String languageCode) {
        return balanceRepository.findByUserIdAndLanguageCodeAndCurrencyType(userId, languageCode, CurrencyType.COINS)
                .map(b -> b.getBalance())
                .orElse(0L);
    }

    public record CreateCoinOrderResponse(String orderId, int amountPaise, String currency,
                                           String razorpayKeyId, String packageCode) {}

    public record CoinCreditResponse(String packageCode, long coinsAwarded, long newCoinBalance) {}
}
