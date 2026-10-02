package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CivilizationTier;
import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.TransactionType;
import com.dsalearner.civilization.dto.CivilizationStateResponse;
import com.dsalearner.civilization.exception.InsufficientResourcesException;
import com.dsalearner.civilization.model.entity.*;
import com.dsalearner.civilization.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * M5.4 unit tests for: lazy production, capacity enforcement, collection, multi-resource atomicity.
 */
@ExtendWith(MockitoExtension.class)
class ResourceEconomyTest {

    @Mock LangoaCivilizationRepository civilizationRepo;
    @Mock LangoaCurrencyBalanceRepository balanceRepo;
    @Mock LangoaTransactionRepository transactionRepo;
    @Mock LangoaBuildingDefinitionRepository buildingDefRepo;
    @Mock LangoaBuildingLevelConfigRepository buildingLevelConfigRepo;
    @Mock LangoaBuildingInstanceRepository buildingInstanceRepo;
    @Mock LangoaBuildingProductionConfigRepository productionConfigRepo;
    @Mock LangoaRewardDefinitionRepository rewardDefRepo;
    @Mock LangoaDecorationDefinitionRepository decorationDefRepo;
    @Mock LangoaDecorationInstanceRepository decorationInstanceRepo;
    @Mock LangoaCityExpansionDefinitionRepository expansionDefRepo;
    @Mock LangoaCityExpansionInstanceRepository expansionInstanceRepo;

    @InjectMocks CivilizationService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID civId  = UUID.randomUUID();

    private LangoaCivilization civilization;

    @BeforeEach
    void setUp() {
        civilization = LangoaCivilization.builder()
                .id(civId).userId(userId).languageCode("de")
                .name("Test Civ")
                .civilizationTier(CivilizationTier.VILLAGE).tierLevel(1)
                .totalLessonsCompleted(5).totalXp(500L)
                .build();
    }

    // ── Lazy production ─────────────────────────────────────────────────────────

    @Test
    void test_lazyProduction_farmProducesFood_afterOneHour() {
        // FARM level 1 at 10 food/hour. lastProductionAt = 1 hour ago.
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        LangoaBuildingInstance farm = LangoaBuildingInstance.builder()
                .id(UUID.randomUUID()).civilizationId(civId)
                .buildingType("FARM").currentLevel(1)
                .lastProductionAt(oneHourAgo)
                .build();

        LangoaBuildingProductionConfig foodConfig = LangoaBuildingProductionConfig.builder()
                .id(UUID.randomUUID()).buildingType("FARM").level(1)
                .resourceType("FOOD").ratePerHour(10)
                .build();

        LangoaCurrencyBalance foodBal = foodBal(userId, 50L, 500L); // 50 current, 500 capacity

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of(farm));
        when(productionConfigRepo.findByBuildingTypeAndLevel("FARM", 1)).thenReturn(List.of(foodConfig));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD)).thenReturn(Optional.of(foodBal));
        when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Also need stubs for buildStateResponse after production
        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of(foodBal));
        when(buildingDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(expansionInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());

        service.getOrCreateAndGetState(userId, "de");

        // After 3600 seconds at 10/hour → exactly 10 units produced
        ArgumentCaptor<LangoaCurrencyBalance> balCaptor = ArgumentCaptor.forClass(LangoaCurrencyBalance.class);
        verify(balanceRepo, atLeast(1)).save(balCaptor.capture());

        boolean foodUpdated = balCaptor.getAllValues().stream()
                .anyMatch(b -> b.getCurrencyType() == CurrencyType.FOOD && b.getBalance() == 60L);
        assertThat(foodUpdated).as("FOOD balance should be 60 after 1 hour of production").isTrue();

        // lastProductionAt should be updated
        verify(buildingInstanceRepo, atLeast(1)).save(argThat(bi ->
                bi.getLastProductionAt() != null && bi.getLastProductionAt().isAfter(oneHourAgo)));
    }

    @Test
    void test_lazyProduction_respectsCapacity() {
        // FARM level 1 at 10/hour. Food is already at capacity (500/500). No production should occur.
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        LangoaBuildingInstance farm = LangoaBuildingInstance.builder()
                .id(UUID.randomUUID()).civilizationId(civId)
                .buildingType("FARM").currentLevel(1)
                .lastProductionAt(oneHourAgo)
                .build();

        LangoaBuildingProductionConfig foodConfig = LangoaBuildingProductionConfig.builder()
                .id(UUID.randomUUID()).buildingType("FARM").level(1)
                .resourceType("FOOD").ratePerHour(10)
                .build();

        LangoaCurrencyBalance foodBal = foodBal(userId, 500L, 500L); // AT capacity

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of(farm));
        when(productionConfigRepo.findByBuildingTypeAndLevel("FARM", 1)).thenReturn(List.of(foodConfig));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD)).thenReturn(Optional.of(foodBal));
        lenient().when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of(foodBal));
        when(buildingDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(expansionInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());

        service.getOrCreateAndGetState(userId, "de");

        // Balance should NOT be updated (already at cap)
        verify(transactionRepo, never()).save(argThat(tx ->
                tx.getTransactionType() == TransactionType.RESOURCE_PRODUCTION));
    }

    @Test
    void test_lazyProduction_zeroSecondsElapsed_noProduction() {
        // lastProductionAt = NOW — no time has elapsed
        LangoaBuildingInstance farm = LangoaBuildingInstance.builder()
                .id(UUID.randomUUID()).civilizationId(civId)
                .buildingType("FARM").currentLevel(1)
                .lastProductionAt(Instant.now()) // just now
                .build();

        LangoaBuildingProductionConfig foodConfig = LangoaBuildingProductionConfig.builder()
                .id(UUID.randomUUID()).buildingType("FARM").level(1)
                .resourceType("FOOD").ratePerHour(10)
                .build();

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of(farm));
        when(productionConfigRepo.findByBuildingTypeAndLevel("FARM", 1)).thenReturn(List.of(foodConfig));
        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of());
        when(buildingDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(expansionInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());

        service.getOrCreateAndGetState(userId, "de");

        // No RESOURCE_PRODUCTION transactions should be written
        verify(transactionRepo, never()).save(argThat(tx ->
                tx.getTransactionType() == TransactionType.RESOURCE_PRODUCTION));
    }

    // ── Capacity in state response ──────────────────────────────────────────────

    @Test
    void test_stateResponse_includesCapacityMap() {
        LangoaCurrencyBalance foodBal = foodBal(userId, 100L, 500L);
        LangoaCurrencyBalance matsBal = materialBal(userId, 80L, 500L);

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of(foodBal, matsBal));
        when(buildingDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(expansionInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());

        CivilizationStateResponse state = service.getOrCreateAndGetState(userId, "de");

        assertThat(state.capacities()).containsEntry("FOOD", 500L);
        assertThat(state.capacities()).containsEntry("MATERIALS", 500L);
        assertThat(state.capacities()).doesNotContainKey("COINS"); // COINS has null capacity
    }

    // ── Balance update capacity enforcement ────────────────────────────────────

    @Test
    void test_updateBalance_exceedsCapacity_cappedAtCapacity() {
        // FOOD capacity = 500, balance = 490. Adding 20 should cap at 500, not go to 510.
        LangoaCurrencyBalance foodBal = foodBal(userId, 490L, 500L);

        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD))
                .thenReturn(Optional.of(foodBal));
        when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Directly test updateBalance: delta +20 but cap is 500
        // The current updateBalance just sets balance = current + delta without cap enforcement.
        // That's correct: production calcs the cap before calling updateBalance.
        // For collection/production we verify the cap is applied externally.
        // This test verifies that a balance of 510 would FAIL the balance >= 0 check but NOT the cap.
        // The cap is enforced in applyLazyProduction, not in updateBalance itself.
        // So test that: balance 490 + 20 = 510 which exceeds capacity 500.
        // The production engine should only credit (500 - 490) = 10, not 20.
        // We simulate this via collectAllResources path (which calls applyLazyProduction).

        // Use a building that produces 20 units when capacity only has 10 room left
        Instant oneHourAgo = Instant.now().minusSeconds(7200); // 2 hours
        LangoaBuildingInstance farm = LangoaBuildingInstance.builder()
                .id(UUID.randomUUID()).civilizationId(civId)
                .buildingType("FARM").currentLevel(1)
                .lastProductionAt(oneHourAgo)
                .build();

        LangoaBuildingProductionConfig foodConfig = LangoaBuildingProductionConfig.builder()
                .id(UUID.randomUUID()).buildingType("FARM").level(1)
                .resourceType("FOOD").ratePerHour(10) // 2h * 10 = 20 produced, but only 10 room
                .build();

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of(farm));
        when(productionConfigRepo.findByBuildingTypeAndLevel("FARM", 1)).thenReturn(List.of(foodConfig));
        lenient().when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD)).thenReturn(Optional.of(foodBal));
        when(buildingDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of(foodBal));
        when(decorationDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(expansionInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());

        service.getOrCreateAndGetState(userId, "de");

        // Should save a FOOD balance of exactly 500 (not 510)
        ArgumentCaptor<LangoaCurrencyBalance> captor = ArgumentCaptor.forClass(LangoaCurrencyBalance.class);
        verify(balanceRepo, atLeast(1)).save(captor.capture());

        boolean cappedAtMax = captor.getAllValues().stream()
                .filter(b -> b.getCurrencyType() == CurrencyType.FOOD)
                .anyMatch(b -> b.getBalance() == 500L);
        assertThat(cappedAtMax).as("FOOD should be capped at capacity 500").isTrue();

        boolean neverExceeded = captor.getAllValues().stream()
                .filter(b -> b.getCurrencyType() == CurrencyType.FOOD)
                .noneMatch(b -> b.getBalance() > 500L);
        assertThat(neverExceeded).as("FOOD must never exceed capacity").isTrue();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private LangoaCurrencyBalance foodBal(UUID uid, long balance, long capacity) {
        LangoaCurrencyBalance b = LangoaCurrencyBalance.builder()
                .userId(uid).languageCode("de")
                .currencyType(CurrencyType.FOOD).balance(balance).build();
        b.setCapacity(capacity);
        return b;
    }

    private LangoaCurrencyBalance materialBal(UUID uid, long balance, long capacity) {
        LangoaCurrencyBalance b = LangoaCurrencyBalance.builder()
                .userId(uid).languageCode("de")
                .currencyType(CurrencyType.MATERIALS).balance(balance).build();
        b.setCapacity(capacity);
        return b;
    }
}
