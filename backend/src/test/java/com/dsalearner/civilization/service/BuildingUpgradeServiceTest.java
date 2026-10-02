package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CivilizationTier;
import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.dto.CivilizationStateResponse;
import com.dsalearner.civilization.exception.CivilizationNotFoundException;
import com.dsalearner.civilization.exception.InsufficientResourcesException;
import com.dsalearner.civilization.model.entity.*;
import com.dsalearner.civilization.repository.*;
import com.dsalearner.economy.analytics.EconomyEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingUpgradeServiceTest {

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
    @Mock EconomyEventService economyEventService;

    @InjectMocks CivilizationService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID civId  = UUID.randomUUID();
    private final UUID instanceId = UUID.randomUUID();

    private LangoaCivilization civilization;
    private LangoaBuildingInstance farmInstance;

    @BeforeEach
    void setUp() {
        civilization = LangoaCivilization.builder()
                .id(civId).userId(userId).languageCode("de")
                .civilizationTier(CivilizationTier.VILLAGE).tierLevel(1)
                .totalLessonsCompleted(10).totalXp(1000L)
                .build();

        farmInstance = LangoaBuildingInstance.builder()
                .id(instanceId).civilizationId(civId)
                .buildingType("FARM").currentLevel(1)
                .positionX(3).positionY(4)
                .buildState("BUILT")
                .build();
    }

    // ── upgradeBuilding ───────────────────────────────────────────────────────

    @Test
    void test_upgrade_sufficientResources_succeeds() {
        LangoaBuildingLevelConfig level2cfg = LangoaBuildingLevelConfig.builder()
                .buildingType("FARM").level(2)
                .coinCost(300L).foodCost(0L).materialCost(30L).woodCost(0L)
                .requiredLessonsCompleted(8).requiredXp(0L)
                .build();

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findById(instanceId)).thenReturn(Optional.of(farmInstance));
        when(buildingLevelConfigRepo.findByBuildingTypeAndLevel("FARM", 2)).thenReturn(Optional.of(level2cfg));

        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.COINS))
                .thenReturn(Optional.of(balance(CurrencyType.COINS, 500L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD))
                .thenReturn(Optional.of(balance(CurrencyType.FOOD, 50L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.MATERIALS))
                .thenReturn(Optional.of(balance(CurrencyType.MATERIALS, 100L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.WOOD))
                .thenReturn(Optional.of(balance(CurrencyType.WOOD, 50L)));

        when(balanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buildingInstanceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buildingInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of(farmInstance));
        when(buildingDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(balanceRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(List.of());
        when(decorationDefRepo.findByActiveTrueOrderByDisplayOrder()).thenReturn(List.of());
        when(decorationInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());
        when(expansionInstanceRepo.findByCivilizationId(civId)).thenReturn(List.of());

        CivilizationStateResponse state = service.upgradeBuilding(userId, "de", instanceId);

        assertThat(state).isNotNull();
        assertThat(state.languageCode()).isEqualTo("de");

        verify(buildingInstanceRepo).save(argThat(bi ->
                bi.getCurrentLevel() == 2 && "FARM".equals(bi.getBuildingType())));
        verify(transactionRepo, atLeast(2)).save(any(LangoaTransaction.class));
    }

    @Test
    void test_upgrade_maxLevel_throws() {
        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findById(instanceId)).thenReturn(Optional.of(farmInstance));
        when(buildingLevelConfigRepo.findByBuildingTypeAndLevel("FARM", 2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upgradeBuilding(userId, "de", instanceId))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessageContaining("max level");
    }

    @Test
    void test_upgrade_insufficientCoins_throws() {
        LangoaBuildingLevelConfig level2cfg = LangoaBuildingLevelConfig.builder()
                .buildingType("FARM").level(2)
                .coinCost(300L).foodCost(0L).materialCost(30L).woodCost(0L)
                .requiredLessonsCompleted(8).requiredXp(0L)
                .build();

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findById(instanceId)).thenReturn(Optional.of(farmInstance));
        when(buildingLevelConfigRepo.findByBuildingTypeAndLevel("FARM", 2)).thenReturn(Optional.of(level2cfg));

        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.COINS))
                .thenReturn(Optional.of(balance(CurrencyType.COINS, 50L))); // not enough

        assertThatThrownBy(() -> service.upgradeBuilding(userId, "de", instanceId))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessageContaining("COINS");

        verify(buildingInstanceRepo, never()).save(any());
    }

    @Test
    void test_upgrade_insufficientLessons_throws() {
        LangoaBuildingLevelConfig level2cfg = LangoaBuildingLevelConfig.builder()
                .buildingType("FARM").level(2)
                .coinCost(300L).foodCost(0L).materialCost(30L).woodCost(0L)
                .requiredLessonsCompleted(20).requiredXp(0L) // requires 20, user has 10
                .build();

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findById(instanceId)).thenReturn(Optional.of(farmInstance));
        when(buildingLevelConfigRepo.findByBuildingTypeAndLevel("FARM", 2)).thenReturn(Optional.of(level2cfg));

        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.COINS))
                .thenReturn(Optional.of(balance(CurrencyType.COINS, 500L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.FOOD))
                .thenReturn(Optional.of(balance(CurrencyType.FOOD, 50L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.MATERIALS))
                .thenReturn(Optional.of(balance(CurrencyType.MATERIALS, 100L)));
        when(balanceRepo.findForUpdate(userId, "de", CurrencyType.WOOD))
                .thenReturn(Optional.of(balance(CurrencyType.WOOD, 50L)));

        assertThatThrownBy(() -> service.upgradeBuilding(userId, "de", instanceId))
                .isInstanceOf(InsufficientResourcesException.class)
                .hasMessageContaining("lessons");

        verify(buildingInstanceRepo, never()).save(any());
    }

    @Test
    void test_upgrade_wrongOwner_throws() {
        UUID otherCivId = UUID.randomUUID();
        farmInstance = LangoaBuildingInstance.builder()
                .id(instanceId).civilizationId(otherCivId) // belongs to different civ
                .buildingType("FARM").currentLevel(1).buildState("BUILT").build();

        when(civilizationRepo.findByUserIdAndLanguageCode(userId, "de")).thenReturn(Optional.of(civilization));
        when(buildingInstanceRepo.findById(instanceId)).thenReturn(Optional.of(farmInstance));
        // ownership check fires before level config lookup — no stub needed for buildingLevelConfigRepo

        assertThatThrownBy(() -> service.upgradeBuilding(userId, "de", instanceId))
                .isInstanceOf(CivilizationNotFoundException.class);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private LangoaCurrencyBalance balance(CurrencyType type, long amount) {
        return LangoaCurrencyBalance.builder()
                .userId(userId).languageCode("de")
                .currencyType(type).balance(amount).build();
    }
}
