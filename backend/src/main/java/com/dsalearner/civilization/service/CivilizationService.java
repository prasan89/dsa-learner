package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CivilizationTier;
import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.DifficultyTier;
import com.dsalearner.civilization.domain.TransactionType;
import com.dsalearner.civilization.dto.*;
import com.dsalearner.civilization.exception.CivilizationNotFoundException;
import com.dsalearner.civilization.exception.InsufficientResourcesException;
import com.dsalearner.civilization.model.entity.*;
import com.dsalearner.civilization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CivilizationService {

    // Maps human-readable route names to ISO 639-1 codes
    private static final Map<String, String> LANGUAGE_NAME_TO_CODE = Map.of(
            "german",   "de",
            "french",   "fr",
            "spanish",  "es",
            "korean",   "ko",
            "japanese", "ja",
            "chinese",  "zh",
            "italian",  "it",
            "portuguese", "pt",
            "hindi",    "hi",
            "kannada",  "kn"
    );

    // Tier advancement thresholds (total lessons completed)
    private static final Map<CivilizationTier, Integer> TIER_THRESHOLDS = new LinkedHashMap<>();

    static {
        TIER_THRESHOLDS.put(CivilizationTier.TOWN,    20);
        TIER_THRESHOLDS.put(CivilizationTier.CITY,   100);
        TIER_THRESHOLDS.put(CivilizationTier.KINGDOM, 300);
        TIER_THRESHOLDS.put(CivilizationTier.EMPIRE,  700);
    }

    // Lessons required to unlock each building type
    private static final Map<String, Integer> BUILDING_UNLOCK_THRESHOLDS = Map.of(
            "HOUSE",           0,
            "FARM",            1,
            "LEARNING_CENTER", 2,
            "SCHOOL",          5,
            "MARKET",         10,
            "WORKSHOP",       15
    );

    private final LangoaCivilizationRepository civilizationRepo;
    private final LangoaCurrencyBalanceRepository balanceRepo;
    private final LangoaTransactionRepository transactionRepo;
    private final LangoaBuildingDefinitionRepository buildingDefRepo;
    private final LangoaBuildingLevelConfigRepository buildingLevelConfigRepo;
    private final LangoaBuildingInstanceRepository buildingInstanceRepo;
    private final LangoaRewardDefinitionRepository rewardDefRepo;

    // ── Public API ────────────────────────────────────────────────────────────

    @Transactional
    public LangoaCivilization getOrCreateCivilization(UUID userId, String languageCode) {
        String code = resolveLanguageCode(languageCode);
        return civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseGet(() -> createNewCivilization(userId, code));
    }

    @Transactional(readOnly = true)
    public CivilizationStateResponse getCivilizationState(UUID userId, String languageCode) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseThrow(() -> new CivilizationNotFoundException(
                        "Civilization not found for user=" + userId + " language=" + code));
        return buildStateResponse(civ);
    }

    @Transactional
    public CivilizationStateResponse getOrCreateAndGetState(UUID userId, String languageCode) {
        LangoaCivilization civ = getOrCreateCivilization(userId, languageCode);
        return buildStateResponse(civ);
    }

    @Transactional
    public LessonRewardResponse applyLessonReward(
            UUID userId, String languageCode, UUID lessonId,
            String cefrLevel, String idempotencyKey) {

        // Idempotency check — already rewarded, return empty response
        if (transactionRepo.existsByIdempotencyKey(idempotencyKey)) {
            log.debug("Lesson reward already applied, idempotency_key={}", idempotencyKey);
            String code = resolveLanguageCode(languageCode);
            Map<String, Long> currentBalances = buildBalanceMap(userId, code);
            return new LessonRewardResponse(0, 0, 0, 0, 0, currentBalances, null, false, List.of());
        }

        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = getOrCreateCivilization(userId, code);

        // Resolve reward definition; fall back to generic defaults
        LangoaRewardDefinition reward = rewardDefRepo
                .findByCefrLevelAndDifficultyTier(cefrLevel != null ? cefrLevel.toUpperCase() : "A1", DifficultyTier.STANDARD)
                .orElseGet(() -> defaultReward());

        String sourceRef = lessonId != null ? lessonId.toString() : "unknown";

        // Apply each currency reward
        updateBalance(userId, code, CurrencyType.XP,                 reward.getXpReward(),
                TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-XP");
        updateBalance(userId, code, CurrencyType.COINS,              reward.getCoinReward(),
                TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-COINS");
        updateBalance(userId, code, CurrencyType.FOOD,               reward.getFoodReward(),
                TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-FOOD");
        updateBalance(userId, code, CurrencyType.MATERIALS,          reward.getMaterialReward(),
                TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-MATERIALS");
        updateBalance(userId, code, CurrencyType.CIVILIZATION_POWER, reward.getCivilizationPowerReward(),
                TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-CIV_POWER");

        // Update civilization stats
        civ.setTotalXp(civ.getTotalXp() + reward.getXpReward());
        civ.setTotalLessonsCompleted(civ.getTotalLessonsCompleted() + 1);

        CivilizationTier previousTier = civ.getCivilizationTier();
        advanceTierIfEligible(civ);
        boolean tierUpgraded = civ.getCivilizationTier() != previousTier;
        civilizationRepo.save(civ);

        // Determine newly unlocked buildings
        List<String> unlocked = computeNewlyUnlockedBuildings(
                civ.getTotalLessonsCompleted() - 1,
                civ.getTotalLessonsCompleted());

        Map<String, Long> newBalances = buildBalanceMap(userId, code);

        return new LessonRewardResponse(
                reward.getXpReward(),
                reward.getCoinReward(),
                reward.getFoodReward(),
                reward.getMaterialReward(),
                reward.getCivilizationPowerReward(),
                newBalances,
                tierUpgraded ? civ.getCivilizationTier().name() : null,
                tierUpgraded,
                unlocked
        );
    }

    @Transactional
    public CivilizationStateResponse buildBuilding(UUID userId, String languageCode, BuildBuildingRequest req) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseThrow(() -> new CivilizationNotFoundException(
                        "Civilization not found for user=" + userId + " language=" + code));

        LangoaBuildingLevelConfig config = buildingLevelConfigRepo
                .findByBuildingTypeAndLevel(req.buildingType(), 1)
                .orElseThrow(() -> new InsufficientResourcesException(
                        "Unknown building type: " + req.buildingType()));

        // Verify resources
        long coins     = getBalance(userId, code, CurrencyType.COINS);
        long food      = getBalance(userId, code, CurrencyType.FOOD);
        long materials = getBalance(userId, code, CurrencyType.MATERIALS);

        if (coins < config.getCoinCost()) {
            throw new InsufficientResourcesException(
                    "Insufficient COINS: need " + config.getCoinCost() + ", have " + coins);
        }
        if (food < config.getFoodCost()) {
            throw new InsufficientResourcesException(
                    "Insufficient FOOD: need " + config.getFoodCost() + ", have " + food);
        }
        if (materials < config.getMaterialCost()) {
            throw new InsufficientResourcesException(
                    "Insufficient MATERIALS: need " + config.getMaterialCost() + ", have " + materials);
        }
        if (civ.getTotalLessonsCompleted() < config.getRequiredLessonsCompleted()) {
            throw new InsufficientResourcesException(
                    "Requires " + config.getRequiredLessonsCompleted() + " lessons completed");
        }

        String buildRef = req.buildingType();

        // Deduct resources
        if (config.getCoinCost() > 0) {
            updateBalance(userId, code, CurrencyType.COINS, -config.getCoinCost(),
                    TransactionType.BUILDING_PURCHASE, buildRef, null);
        }
        if (config.getFoodCost() > 0) {
            updateBalance(userId, code, CurrencyType.FOOD, -config.getFoodCost(),
                    TransactionType.BUILDING_PURCHASE, buildRef, null);
        }
        if (config.getMaterialCost() > 0) {
            updateBalance(userId, code, CurrencyType.MATERIALS, -config.getMaterialCost(),
                    TransactionType.BUILDING_PURCHASE, buildRef, null);
        }

        // Create building instance
        LangoaBuildingInstance instance = LangoaBuildingInstance.builder()
                .civilizationId(civ.getId())
                .buildingType(req.buildingType())
                .currentLevel(1)
                .positionX(req.positionX())
                .positionY(req.positionY())
                .build();
        buildingInstanceRepo.save(instance);

        log.info("Built {} for civ={}", req.buildingType(), civ.getId());
        return buildStateResponse(civ);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private LangoaCivilization createNewCivilization(UUID userId, String languageCode) {
        LangoaCivilization civ = LangoaCivilization.builder()
                .userId(userId)
                .languageCode(languageCode)
                .build();
        civ = civilizationRepo.save(civ);

        // Initialize all 6 currency balance rows at 0
        for (CurrencyType ct : CurrencyType.values()) {
            LangoaCurrencyBalance bal = LangoaCurrencyBalance.builder()
                    .userId(userId)
                    .languageCode(languageCode)
                    .currencyType(ct)
                    .balance(0L)
                    .build();
            balanceRepo.save(bal);
        }

        // Apply initial grant
        updateBalance(userId, languageCode, CurrencyType.COINS,   200,
                TransactionType.INITIAL_GRANT, "new_civilization", null);
        updateBalance(userId, languageCode, CurrencyType.FOOD,     50,
                TransactionType.INITIAL_GRANT, "new_civilization", null);
        updateBalance(userId, languageCode, CurrencyType.MATERIALS, 30,
                TransactionType.INITIAL_GRANT, "new_civilization", null);

        // Give starter HOUSE building for free at position (2,2)
        LangoaBuildingInstance house = LangoaBuildingInstance.builder()
                .civilizationId(civ.getId())
                .buildingType("HOUSE")
                .currentLevel(1)
                .positionX(2)
                .positionY(2)
                .build();
        buildingInstanceRepo.save(house);

        log.info("Created new civilization id={} for userId={} language={}", civ.getId(), userId, languageCode);
        return civ;
    }

    private void updateBalance(UUID userId, String languageCode, CurrencyType currencyType,
                               long delta, TransactionType txType, String sourceRef, String idempotencyKey) {
        LangoaCurrencyBalance bal = balanceRepo
                .findByUserIdAndLanguageCodeAndCurrencyType(userId, languageCode, currencyType)
                .orElseGet(() -> {
                    LangoaCurrencyBalance newBal = LangoaCurrencyBalance.builder()
                            .userId(userId)
                            .languageCode(languageCode)
                            .currencyType(currencyType)
                            .balance(0L)
                            .build();
                    return balanceRepo.save(newBal);
                });

        long newBalance = bal.getBalance() + delta;
        if (newBalance < 0) {
            throw new InsufficientResourcesException(
                    "Insufficient " + currencyType + ": balance would be " + newBalance);
        }

        bal.setBalance(newBalance);
        balanceRepo.save(bal);

        LangoaTransaction txn = LangoaTransaction.builder()
                .userId(userId)
                .languageCode(languageCode)
                .transactionType(txType)
                .currencyType(currencyType)
                .amount(delta)
                .balanceAfter(newBalance)
                .sourceReference(sourceRef)
                .idempotencyKey(idempotencyKey)
                .build();
        transactionRepo.save(txn);
    }

    private long getBalance(UUID userId, String languageCode, CurrencyType currencyType) {
        return balanceRepo.findByUserIdAndLanguageCodeAndCurrencyType(userId, languageCode, currencyType)
                .map(LangoaCurrencyBalance::getBalance)
                .orElse(0L);
    }

    private Map<String, Long> buildBalanceMap(UUID userId, String languageCode) {
        List<LangoaCurrencyBalance> balances = balanceRepo.findByUserIdAndLanguageCode(userId, languageCode);
        Map<String, Long> map = new LinkedHashMap<>();
        for (CurrencyType ct : CurrencyType.values()) {
            map.put(ct.name(), 0L);
        }
        for (LangoaCurrencyBalance bal : balances) {
            map.put(bal.getCurrencyType().name(), bal.getBalance());
        }
        return map;
    }

    private CivilizationStateResponse buildStateResponse(LangoaCivilization civ) {
        Map<String, Long> balances = buildBalanceMap(civ.getUserId(), civ.getLanguageCode());

        List<LangoaBuildingInstance> instances = buildingInstanceRepo.findByCivilizationId(civ.getId());

        // Build display name map for building types
        Map<String, String> defDisplayNames = buildingDefRepo.findByActiveTrueOrderByDisplayOrder()
                .stream()
                .collect(Collectors.toMap(
                        LangoaBuildingDefinition::getBuildingType,
                        LangoaBuildingDefinition::getDisplayName));

        List<BuildingInstanceDto> buildings = instances.stream()
                .map(inst -> new BuildingInstanceDto(
                        inst.getId(),
                        inst.getBuildingType(),
                        defDisplayNames.getOrDefault(inst.getBuildingType(), inst.getBuildingType()),
                        inst.getCurrentLevel(),
                        inst.getPositionX(),
                        inst.getPositionY()))
                .toList();

        return new CivilizationStateResponse(
                civ.getId(),
                civ.getLanguageCode(),
                civ.getName(),
                civ.getCivilizationTier().name(),
                civ.getTierLevel(),
                civ.getTotalXp(),
                civ.getTotalLessonsCompleted(),
                balances,
                buildings
        );
    }

    private void advanceTierIfEligible(LangoaCivilization civ) {
        int lessons = civ.getTotalLessonsCompleted();
        // Ordered list of tiers beyond VILLAGE
        CivilizationTier[] progression = {
                CivilizationTier.EMPIRE,
                CivilizationTier.KINGDOM,
                CivilizationTier.CITY,
                CivilizationTier.TOWN
        };
        for (CivilizationTier tier : progression) {
            Integer threshold = TIER_THRESHOLDS.get(tier);
            if (threshold != null && lessons >= threshold && civ.getCivilizationTier().ordinal() < tier.ordinal()) {
                civ.setCivilizationTier(tier);
                log.info("Civilization {} advanced to {}", civ.getId(), tier);
                return;
            }
        }
    }

    private List<String> computeNewlyUnlockedBuildings(int lessonsBefore, int lessonsAfter) {
        return BUILDING_UNLOCK_THRESHOLDS.entrySet().stream()
                .filter(e -> e.getValue() > lessonsBefore && e.getValue() <= lessonsAfter)
                .map(Map.Entry::getKey)
                .toList();
    }

    private LangoaRewardDefinition defaultReward() {
        LangoaRewardDefinition def = new LangoaRewardDefinition();
        def.setDifficultyTier(DifficultyTier.STANDARD);
        def.setXpReward(100);
        def.setCoinReward(50);
        def.setFoodReward(10);
        def.setMaterialReward(5);
        def.setCivilizationPowerReward(100);
        return def;
    }

    public String resolveLanguageCode(String languageCode) {
        if (languageCode == null) return "de";
        String isoCode = LANGUAGE_NAME_TO_CODE.get(languageCode.toLowerCase());
        return isoCode != null ? isoCode : languageCode.toLowerCase();
    }

    public List<BuildingDefinitionDto> getBuildingDefinitions(UUID userId, String languageCode) {
        String code = resolveLanguageCode(languageCode);
        Map<String, Long> balances = buildBalanceMap(userId, code);
        long coins     = balances.getOrDefault(CurrencyType.COINS.name(),     0L);
        long food      = balances.getOrDefault(CurrencyType.FOOD.name(),      0L);
        long materials = balances.getOrDefault(CurrencyType.MATERIALS.name(), 0L);

        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseGet(() -> createNewCivilization(userId, code));
        int totalLessons = civ.getTotalLessonsCompleted();

        return buildingDefRepo.findByActiveTrueOrderByDisplayOrder().stream()
                .map(def -> {
                    List<LangoaBuildingLevelConfig> levelConfigs =
                            buildingLevelConfigRepo.findByBuildingType(def.getBuildingType());
                    List<BuildingLevelConfigDto> levelDtos = levelConfigs.stream()
                            .sorted(Comparator.comparingInt(LangoaBuildingLevelConfig::getLevel))
                            .map(cfg -> {
                                boolean affordable =
                                        coins >= cfg.getCoinCost() &&
                                        food >= cfg.getFoodCost() &&
                                        materials >= cfg.getMaterialCost() &&
                                        totalLessons >= cfg.getRequiredLessonsCompleted();
                                return new BuildingLevelConfigDto(
                                        cfg.getLevel(),
                                        cfg.getDisplayName(),
                                        cfg.getCoinCost(),
                                        cfg.getFoodCost(),
                                        cfg.getMaterialCost(),
                                        cfg.getRequiredLessonsCompleted(),
                                        cfg.getRequiredXp(),
                                        affordable);
                            })
                            .toList();
                    return new BuildingDefinitionDto(
                            def.getId(),
                            def.getBuildingType(),
                            def.getDisplayName(),
                            def.getDescription(),
                            def.getMaxLevel(),
                            def.getAssetRef(),
                            levelDtos);
                })
                .toList();
    }
}
