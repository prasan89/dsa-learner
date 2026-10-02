package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.entity.LangCoinPackage;
import com.dsalearner.civilization.repository.LangCoinPackageRepository;
import com.dsalearner.civilization.repository.LangoaCurrencyBalanceRepository;
import com.dsalearner.subscription.entity.PaymentEvent;
import com.dsalearner.subscription.repository.PaymentEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoinPurchaseServiceTest {

    @Mock LangCoinPackageRepository coinPackageRepository;
    @Mock PaymentEventRepository paymentEventRepository;
    @Mock CivilizationService civilizationService;
    @Mock LangoaCurrencyBalanceRepository balanceRepository;

    @InjectMocks CoinPurchaseService service;

    private final UUID userId = UUID.randomUUID();
    private final String orderId = "order_test123";
    private final String packageCode = "COINS_250";
    private final String languageCode = "de";

    private LangCoinPackage pkg250;

    @BeforeEach
    void setUp() {
        pkg250 = LangCoinPackage.builder()
                .id(UUID.randomUUID())
                .packageCode(packageCode)
                .displayName("Small Pouch")
                .coinAmount(250)
                .pricePaise(2900)
                .currency("INR")
                .playProductId("langoa_coins_250")
                .isActive(true)
                .build();
    }

    @Test
    void verifyPlayAndCredit_newOrder_creditsCoinsExactly() {
        when(coinPackageRepository.findByPackageCodeAndIsActiveTrue(packageCode))
                .thenReturn(Optional.of(pkg250));
        when(paymentEventRepository.existsByPlayOrderIdAndStatus(orderId, PaymentEvent.EventStatus.PROCESSED))
                .thenReturn(false);
        when(balanceRepository.findByUserIdAndLanguageCodeAndCurrencyType(userId, languageCode, CurrencyType.COINS))
                .thenReturn(Optional.empty());

        CoinPurchaseService.CoinCreditResponse response =
                service.verifyPlayAndCredit(userId, "token_abc", orderId, packageCode, languageCode);

        assertThat(response.coinsAwarded()).isEqualTo(250);
        verify(civilizationService).updateBalancePublic(
                eq(userId), eq(languageCode), eq(CurrencyType.COINS),
                eq(250L), any(), eq(orderId), eq("coin-purchase-" + orderId));
    }

    @Test
    void verifyPlayAndCredit_duplicateOrder_isIdempotent() {
        when(paymentEventRepository.existsByPlayOrderIdAndStatus(orderId, PaymentEvent.EventStatus.PROCESSED))
                .thenReturn(true);
        when(balanceRepository.findByUserIdAndLanguageCodeAndCurrencyType(userId, languageCode, CurrencyType.COINS))
                .thenReturn(Optional.empty());

        CoinPurchaseService.CoinCreditResponse response =
                service.verifyPlayAndCredit(userId, "token_dup", orderId, packageCode, languageCode);

        assertThat(response.coinsAwarded()).isEqualTo(0);
        verify(civilizationService, never()).updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void createOrder_unknownPackage_throws() {
        when(coinPackageRepository.findByPackageCodeAndIsActiveTrue("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createOrder(userId, "UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown coin package");
    }

    @Test
    void getPackages_returnsActivePackages() {
        when(coinPackageRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(java.util.List.of(pkg250));

        var packages = service.getPackages();

        assertThat(packages).hasSize(1);
        assertThat(packages.get(0).packageCode()).isEqualTo(packageCode);
        assertThat(packages.get(0).coinAmount()).isEqualTo(250);
    }
}
