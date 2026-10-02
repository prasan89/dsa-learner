package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CivilizationTier;
import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.DifficultyTier;
import com.dsalearner.civilization.domain.TransactionType;
import com.dsalearner.civilization.dto.BuildBuildingRequest;
import com.dsalearner.civilization.dto.CivilizationStateResponse;
import com.dsalearner.civilization.dto.LessonRewardResponse;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CivilizationService using Mockito.
 *
 * The tests cover:
 * - applyLessonReward: first-time reward and duplicate idempotency
 * - buildBuilding: success path, insufficient coins, insufficient lessons
 * - getOrCreateCivilization: new user path, including COINS initial grant
 */
@ExtendWith(MockitoExtension.class)
class CivilizationServiceTest {

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

    private final UUID userId    = UUID.randomUUID();
    private final UUID civId     = UUID.randomUUID();
    private final UUID lessonId  = UUID.randomUUID();

    private LangoaCivilization civilization;
    private LangoaRewardDefinition rewardA1;

    @BeforeEach
    void setUp() {
        civilization = LangoaCivilization.builder()
                .id(civId)
                .userId(userId)
                .languageCode("de")
                .name("Test Civ")
                .civilizationTier(CivilizationTier.VILLAGE)
                .tierLevel(1)
                .totalLessonsCompleted(0)
                .totalXp(0L)
                .build();

        rewardA1 = new LangoaRewardDefinition();
        rewardA1.setDifficultyTier(DifficultyTier.STANDARD);
        rewardA1.setXpReward(100);
        rewardA1.setCoinReward(50);
        rewardA1.setFoodReward(10);
        rewardA1.setMaterialReward(5);
        rewardA1.setCivilizationPowerReward(100);
    }

    // ── applyLessonReward ────────────────────────────────────────────────────

    @Test
    void test_applyLessonReward_firstTime_grantsReward() {
        // Idempotency key does NOT exist yet
        when(transactionRepo.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(Optional.of(civilization));
        when(rewardDefRepo.findByCefrLevelAndDifficultyTier("A1", DifficultyTier.STANDARD))
                .thenReturn(Optional.of(rewardA1));

        // updateBalance now calls findForUpdate (SELECT FOR UPDATE) for writes
        when(balanceRepo.findForUpdate(eq(userId), eq("de"), any()))
                .thenAnswer(inv -> {
                    CurrencyType ct = inv.getArgument(2);
                    LangoaCurrencyBalance bal = LangoaCurrencyBalance.builder()
                            .userId(userId).languageCode("de")
                            .currencyType(ct).balance(0L).build();
                    return Optional.of(bal);
                });
        when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(civilizationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of());

        LessonRewardResponse response = service.applyLessonReward(
                userId, "de", lessonId, "A1", "idem-key-001");

        assertThat(response.xpEarned()).isEqualTo(100);
        assertThat(response.coinsEarned()).isEqualTo(50);
        assertThat(response.foodEarned()).isEqualTo(10);
        assertThat(response.materialsEarned()).isEqualTo(5);
        assertThat(response.woodEarned()).isEqualTo(5); // field default on LangoaRewardDefinition is 5L
        assertThat(response.civilizationPowerEarned()).isEqualTo(100);
        assertThat(response.tierUpgraded()).isFalse();

        // Verify transactions were persisted (one per currency = 6 calls: XP, COINS, FOOD, MATERIALS, WOOD, CIV_POWER)
        verify(transactionRepo, times(6)).save(any(LangoaTransaction.class));
        verify(civilizationRepo).save(any(LangoaCivilization.class));
    }

    @Test
    void test_applyLessonReward_duplicate_isIdempotent() {
        // Idempotency key ALREADY exists
        when(transactionRepo.existsByIdempotencyKey(anyString())).thenReturn(true);

        LessonRewardResponse response = service.applyLessonReward(
                userId, "de", lessonId, "A1", "idem-key-duplicate");

        // Response should have 0 for all reward amounts (already rewarded)
        assertThat(response.xpEarned()).isEqualTo(0);
        assertThat(response.coinsEarned()).isEqualTo(0);
        assertThat(response.foodEarned()).isEqualTo(0);
        assertThat(response.materialsEarned()).isEqualTo(0);
        assertThat(response.tierUpgraded()).isFalse();

        // No balances updated and no new transactions persisted
        verify(transactionRepo, never()).save(any());
        verify(balanceRepo, never()).save(any());
        verify(civilizationRepo, never()).save(any());
    }

    @Test
    void test_applyLessonReward_concurrentDuplicate_dbConstraintFired_returnsZeroReward() {
        // Simulate concurrent duplicate: fast-path check returns false (race condition),
        // but when the first updateBalance call tries to save the transaction with the
        // UNIQUE idempotency_key, the DB constraint fires a DataIntegrityViolationException.
        when(transactionRepo.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(Optional.of(civilization));
        when(rewardDefRepo.findByCefrLevelAndDifficultyTier("A1", DifficultyTier.STANDARD))
                .thenReturn(Optional.of(rewardA1));
        when(balanceRepo.findForUpdate(eq(userId), eq("de"), any()))
                .thenAnswer(inv -> {
                    CurrencyType ct = inv.getArgument(2);
                    return Optional.of(LangoaCurrencyBalance.builder()
                            .userId(userId).languageCode("de")
                            .currencyType(ct).balance(0L).build());
                });
        when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // First transactionRepo.save() throws — simulates the concurrent duplicate losing the DB race
        when(transactionRepo.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key: idempotency_key"));
        lenient().when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of());

        LessonRewardResponse response = service.applyLessonReward(
                userId, "de", lessonId, "A1", "idem-key-concurrent");

        // The concurrent loser must return zero reward — never double-credit
        assertThat(response.xpEarned()).isEqualTo(0);
        assertThat(response.coinsEarned()).isEqualTo(0);
        assertThat(response.tierUpgraded()).isFalse();

        // Civilization stats must NOT be incremented by the losing request
        verify(civilizationRepo, never()).save(any());
    }

    // ── buildBuilding ────────────────────────────────────────────────────────

    @Test
    void test_buildBuilding_sufficientResources_succeeds() {
        // FARM costs 100 coins, 10 materials, 0 food, requires 1 lesson
        LangoaBuildingLevelConfig farmCfg = LangoaBuildingLevelConfig.builder()
                .buildingType("FARM").level(1)
                .coinCost(100L).foodCost(0L).materialCost(10L)
                .requiredLessonsCompleted(1)
                .build();

        civilization.setTotalLessonsCompleted(5); // enough lessons

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(Optional.of(civilization));
        when(buildingLevelConfigRepo.findByBuildingTypeAndLevel("FARM", 1))
                .thenReturn(Optional.of(farmCfg));

        // buildBuilding uses getBalanceForUpdate → findForUpdate for pre-check
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.COINS))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.COINS, 200L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.FOOD, 50L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.MATERIALS))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.MATERIALS, 30L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.WOOD))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.WOOD, 20L)));

        when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buildingInstanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buildingInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(buildingDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of());
        when(decorationDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(expansionInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());

        BuildBuildingRequest req = new BuildBuildingRequest("FARM", 3, 4);
        CivilizationStateResponse state = service.buildBuilding(userId, "de", req);

        assertThat(state).isNotNull();
        assertThat(state.languageCode()).isEqualTo("de");

        // Verify building instance was saved
        ArgumentCaptor<LangoaBuildingInstance> instanceCaptor =
                ArgumentCaptor.forClass(LangoaBuildingInstance.class);
        verify(buildingInstanceRepo).save(instanceCaptor.capture());
        assertThat(instanceCaptor.getValue().getBuildingType()).isEqualTo("FARM");
        assertThat(instanceCaptor.getValue().getPositionX()).isEqualTo(3);
        assertThat(instanceCaptor.getValue().getPositionY()).isEqualTo(4);

        // Coins and materials deducted (BUILDING_PURCHASE transactions)
        verify(transactionRepo, atLeast(2)).save(any(LangoaTransaction.class));
    }

    @Test
    void test_buildBuilding_insufficientCoins_throws() {
        LangoaBuildingLevelConfig farmCfg = LangoaBuildingLevelConfig.builder()
                .buildingType("FARM").level(1)
                .coinCost(100L).foodCost(0L).materialCost(10L)
                .requiredLessonsCompleted(1)
                .build();

        civilization.setTotalLessonsCompleted(5);

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(Optional.of(civilization));
        when(buildingLevelConfigRepo.findByBuildingTypeAndLevel("FARM", 1))
                .thenReturn(Optional.of(farmCfg));

        // User has 0 coins — not enough; FOOD/MATERIALS are never checked so no stubs needed
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.COINS))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.COINS, 0L)));

        BuildBuildingRequest req = new BuildBuildingRequest("FARM", 0, 0);

        assertThatThrownBy(() -> service.buildBuilding(userId, "de", req))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessageContaining("COINS");

        verify(buildingInstanceRepo, never()).save(any());
    }

    @Test
    void test_buildBuilding_insufficientLessons_throws() {
        LangoaBuildingLevelConfig farmCfg = LangoaBuildingLevelConfig.builder()
                .buildingType("FARM").level(1)
                .coinCost(100L).foodCost(0L).materialCost(10L)
                .requiredLessonsCompleted(1)
                .build();

        // User has 0 lessons completed — FARM requires 1
        civilization.setTotalLessonsCompleted(0);

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(Optional.of(civilization));
        when(buildingLevelConfigRepo.findByBuildingTypeAndLevel("FARM", 1))
                .thenReturn(Optional.of(farmCfg));

        // User has plenty of coins and materials — fails on lesson count gate
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.COINS))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.COINS, 500L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.FOOD, 50L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.MATERIALS))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.MATERIALS, 30L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.WOOD))
                .thenReturn(Optional.of(balanceOf(userId, CurrencyType.WOOD, 20L)));

        BuildBuildingRequest req = new BuildBuildingRequest("FARM", 0, 0);

        assertThatThrownBy(() -> service.buildBuilding(userId, "de", req))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessageContaining("lessons");

        verify(buildingInstanceRepo, never()).save(any());
    }

    // ── getOrCreateCivilization ───────────────────────────────────────────────

    @Test
    void test_getCivilizationState_newUser_createsWithInitialGrant() {
        // No existing civilization
        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(Optional.empty());

        // Save returns the new civ with a generated ID
        LangoaCivilization savedCiv = LangoaCivilization.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .languageCode("de")
                .name("My Civilization")
                .civilizationTier(CivilizationTier.VILLAGE)
                .tierLevel(1)
                .totalLessonsCompleted(0)
                .totalXp(0L)
                .build();
        when(civilizationRepo.save(any())).thenReturn(savedCiv);

        // Balance creation: return empty optional so new balances are created via findForUpdate (initial grant path)
        when(balanceRepo.findForUpdate(eq(userId), eq("de"), any()))
                .thenReturn(Optional.empty());
        when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buildingInstanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // This triggers getOrCreateCivilization internally
        LangoaCivilization result = service.getOrCreateCivilization(userId, "de");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getLanguageCode()).isEqualTo("de");

        // Civilization saved once
        verify(civilizationRepo).save(any(LangoaCivilization.class));

        // 7 balance rows created (one per CurrencyType: COINS, GEMS, XP, FOOD, MATERIALS, WOOD, CIVILIZATION_POWER)
        verify(balanceRepo, atLeast(7)).save(any(LangoaCurrencyBalance.class));

        // Initial grant transactions: COINS(200), FOOD(50), MATERIALS(30) = 3 calls minimum
        ArgumentCaptor<LangoaTransaction> txnCaptor =
                ArgumentCaptor.forClass(LangoaTransaction.class);
        verify(transactionRepo, atLeast(3)).save(txnCaptor.capture());

        List<LangoaTransaction> transactions = txnCaptor.getAllValues();
        boolean hasCoinsGrant = transactions.stream()
                .anyMatch(t -> t.getTransactionType() == TransactionType.INITIAL_GRANT
                            && t.getCurrencyType() == CurrencyType.COINS
                            && t.getAmount() == 200);
        assertThat(hasCoinsGrant)
                .as("Should have an INITIAL_GRANT of 200 COINS")
                .isTrue();

        // Starter HOUSE building saved
        verify(buildingInstanceRepo).save(argThat(bi ->
                "HOUSE".equals(bi.getBuildingType())
                && bi.getCurrentLevel() == 1
                && bi.getPositionX() == 2
                && bi.getPositionY() == 2));
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private LangoaCurrencyBalance balanceOf(UUID uid, CurrencyType type, long amount) {
        return LangoaCurrencyBalance.builder()
                .userId(uid)
                .languageCode("de")
                .currencyType(type)
                .balance(amount)
                .build();
    }
}
