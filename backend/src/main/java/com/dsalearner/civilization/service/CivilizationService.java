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
import org.springframework.dao.DataIntegrityViolationException;
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
            "german",     "de",
            "french",     "fr",
            "spanish",    "es",
            "korean",     "ko",
            "japanese",   "ja",
            "chinese",    "zh",
            "italian",    "it",
            "portuguese", "pt",
            "hindi",      "hi",
            "kannada",    "kn"
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
            "WORKSHOP",       15,
            "PARK",           20,
            "LIBRARY",        30
    );

    private final LangoaCivilizationRepository civilizationRepo;
    private final LangoaCurrencyBalanceRepository balanceRepo;
    private final LangoaTransactionRepository transactionRepo;
    private final LangoaBuildingDefinitionRepository buildingDefRepo;
    private final LangoaBuildingLevelConfigRepository buildingLevelConfigRepo;
    private final LangoaBuildingInstanceRepository buildingInstanceRepo;
    private final LangoaRewardDefinitionRepository rewardDefRepo;
    private final LangoaDecorationDefinitionRepository decorationDefRepo;
    private final LangoaDecorationInstanceRepository decorationInstanceRepo;
    private final LangoaCityExpansionDefinitionRepository expansionDefRepo;
    private final LangoaCityExpansionInstanceRepository expansionInstanceRepo;

    // ── Public API ────────────────────────────────────────────────────────────

    @Transactional
    public LangoaCivilization getOrCreateCivilization(UUID userId, String languageCode) {
        String code = resolveLanguageCode(languageCode);
        return civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseGet(() -> {
                    try {
                        return createNewCivilization(userId, code);
                    } catch (DataIntegrityViolationException e) {
                        // Concurrent first-time request already created the row — just fetch it
                        return civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Civilization disappeared after concurrent creation for user=" + userId));
                    }
                });
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

        String code = resolveLanguageCode(languageCode);

        // Fast path: check before issuing all the locks (non-authoritative — race is resolved by the
        // UNIQUE constraint on idempotency_key when the transaction actually commits)
        if (idempotencyKey != null && transactionRepo.existsByIdempotencyKey(idempotencyKey)) {
            log.debug("Lesson reward already applied, idempotency_key={}", idempotencyKey);
            Map<String, Long> currentBalances = buildBalanceMap(userId, code);
            return new LessonRewardResponse(0, 0, 0, 0, 0, 0, currentBalances, null, false, List.of());
        }

        LangoaCivilization civ = getOrCreateCivilization(userId, code);

        // Resolve reward definition; fall back to generic defaults
        LangoaRewardDefinition reward = rewardDefRepo
                .findByCefrLevelAndDifficultyTier(cefrLevel != null ? cefrLevel.toUpperCase() : "A1", DifficultyTier.STANDARD)
                .orElseGet(this::defaultReward);

        String sourceRef = lessonId != null ? lessonId.toString() : "unknown";

        try {
            // Apply each currency reward — updateBalance uses SELECT FOR UPDATE on balance rows
            updateBalance(userId, code, CurrencyType.XP,                 reward.getXpReward(),
                    TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-XP");
            updateBalance(userId, code, CurrencyType.COINS,              reward.getCoinReward(),
                    TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-COINS");
            updateBalance(userId, code, CurrencyType.FOOD,               reward.getFoodReward(),
                    TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-FOOD");
            updateBalance(userId, code, CurrencyType.MATERIALS,          reward.getMaterialReward(),
                    TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-MATERIALS");
            updateBalance(userId, code, CurrencyType.WOOD,               reward.getWoodReward(),
                    TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-WOOD");
            updateBalance(userId, code, CurrencyType.CIVILIZATION_POWER, reward.getCivilizationPowerReward(),
                    TransactionType.LESSON_COMPLETION, sourceRef, idempotencyKey + "-CIV_POWER");
        } catch (DataIntegrityViolationException e) {
            // Idempotency_key UNIQUE constraint fired — concurrent duplicate request lost the race
            log.debug("Duplicate lesson reward blocked by DB constraint, idempotency_key={}", idempotencyKey);
            Map<String, Long> currentBalances = buildBalanceMap(userId, code);
            return new LessonRewardResponse(0, 0, 0, 0, 0, 0, currentBalances, null, false, List.of());
        }

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
                reward.getWoodReward(),
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

        // Verify resources — SELECT FOR UPDATE prevents concurrent builds from passing with the same balance
        long coins     = getBalanceForUpdate(userId, code, CurrencyType.COINS);
        long food      = getBalanceForUpdate(userId, code, CurrencyType.FOOD);
        long materials = getBalanceForUpdate(userId, code, CurrencyType.MATERIALS);
        long wood      = getBalanceForUpdate(userId, code, CurrencyType.WOOD);

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
        if (wood < config.getWoodCost()) {
            throw new InsufficientResourcesException(
                    "Insufficient WOOD: need " + config.getWoodCost() + ", have " + wood);
        }
        if (civ.getTotalLessonsCompleted() < config.getRequiredLessonsCompleted()) {
            throw new InsufficientResourcesException(
                    "Requires " + config.getRequiredLessonsCompleted() + " lessons completed");
        }
        if (civ.getTotalXp() < config.getRequiredXp()) {
            throw new InsufficientResourcesException(
                    "Requires " + config.getRequiredXp() + " total XP");
        }

        String buildRef = req.buildingType();

        // Deduct resources
        if (config.getCoinCost() > 0)
            updateBalance(userId, code, CurrencyType.COINS, -config.getCoinCost(),
                    TransactionType.BUILDING_PURCHASE, buildRef, null);
        if (config.getFoodCost() > 0)
            updateBalance(userId, code, CurrencyType.FOOD, -config.getFoodCost(),
                    TransactionType.BUILDING_PURCHASE, buildRef, null);
        if (config.getMaterialCost() > 0)
            updateBalance(userId, code, CurrencyType.MATERIALS, -config.getMaterialCost(),
                    TransactionType.BUILDING_PURCHASE, buildRef, null);
        if (config.getWoodCost() > 0)
            updateBalance(userId, code, CurrencyType.WOOD, -config.getWoodCost(),
                    TransactionType.BUILDING_PURCHASE, buildRef, null);

        // Create building instance
        LangoaBuildingInstance instance = LangoaBuildingInstance.builder()
                .civilizationId(civ.getId())
                .buildingType(req.buildingType())
                .currentLevel(1)
                .positionX(req.positionX())
                .positionY(req.positionY())
                .buildState("BUILT")
                .build();
        buildingInstanceRepo.save(instance);

        log.info("Built {} for civ={}", req.buildingType(), civ.getId());
        return buildStateResponse(civ);
    }

    @Transactional
    public CivilizationStateResponse upgradeBuilding(UUID userId, String languageCode, UUID buildingInstanceId) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseThrow(() -> new CivilizationNotFoundException(
                        "Civilization not found for user=" + userId));

        LangoaBuildingInstance instance = buildingInstanceRepo.findById(buildingInstanceId)
                .orElseThrow(() -> new InsufficientResourcesException("Building instance not found: " + buildingInstanceId));

        // Validate ownership
        if (!instance.getCivilizationId().equals(civ.getId())) {
            throw new CivilizationNotFoundException("Building does not belong to this civilization");
        }

        int nextLevel = instance.getCurrentLevel() + 1;
        LangoaBuildingLevelConfig config = buildingLevelConfigRepo
                .findByBuildingTypeAndLevel(instance.getBuildingType(), nextLevel)
                .orElseThrow(() -> new InsufficientResourcesException(
                        instance.getBuildingType() + " is already at max level " + instance.getCurrentLevel()));

        // SELECT FOR UPDATE on all three spending currencies
        long coins     = getBalanceForUpdate(userId, code, CurrencyType.COINS);
        long food      = getBalanceForUpdate(userId, code, CurrencyType.FOOD);
        long materials = getBalanceForUpdate(userId, code, CurrencyType.MATERIALS);
        long wood      = getBalanceForUpdate(userId, code, CurrencyType.WOOD);

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
        if (wood < config.getWoodCost()) {
            throw new InsufficientResourcesException(
                    "Insufficient WOOD: need " + config.getWoodCost() + ", have " + wood);
        }
        if (civ.getTotalLessonsCompleted() < config.getRequiredLessonsCompleted()) {
            throw new InsufficientResourcesException(
                    "Requires " + config.getRequiredLessonsCompleted() + " lessons to upgrade to level " + nextLevel);
        }
        if (civ.getTotalXp() < config.getRequiredXp()) {
            throw new InsufficientResourcesException(
                    "Requires " + config.getRequiredXp() + " total XP to upgrade to level " + nextLevel);
        }

        String upgradeRef = instance.getBuildingType() + "_L" + nextLevel;

        if (config.getCoinCost() > 0)
            updateBalance(userId, code, CurrencyType.COINS, -config.getCoinCost(),
                    TransactionType.BUILDING_UPGRADE, upgradeRef, null);
        if (config.getFoodCost() > 0)
            updateBalance(userId, code, CurrencyType.FOOD, -config.getFoodCost(),
                    TransactionType.BUILDING_UPGRADE, upgradeRef, null);
        if (config.getMaterialCost() > 0)
            updateBalance(userId, code, CurrencyType.MATERIALS, -config.getMaterialCost(),
                    TransactionType.BUILDING_UPGRADE, upgradeRef, null);
        if (config.getWoodCost() > 0)
            updateBalance(userId, code, CurrencyType.WOOD, -config.getWoodCost(),
                    TransactionType.BUILDING_UPGRADE, upgradeRef, null);

        instance.setCurrentLevel(nextLevel);
        instance.setUpgradedAt(java.time.Instant.now());
        instance.setBuildState("BUILT");
        buildingInstanceRepo.save(instance);

        log.info("Upgraded {} to level {} for civ={}", instance.getBuildingType(), nextLevel, civ.getId());
        return buildStateResponse(civ);
    }

    @Transactional
    public CivilizationStateResponse moveBuilding(UUID userId, String languageCode, UUID buildingInstanceId, MoveBuildingRequest req) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseThrow(() -> new CivilizationNotFoundException("Civilization not found for user=" + userId));

        LangoaBuildingInstance instance = buildingInstanceRepo.findById(buildingInstanceId)
                .orElseThrow(() -> new InsufficientResourcesException("Building instance not found: " + buildingInstanceId));

        if (!instance.getCivilizationId().equals(civ.getId())) {
            throw new CivilizationNotFoundException("Building does not belong to this civilization");
        }

        instance.setPositionX(req.positionX());
        instance.setPositionY(req.positionY());
        instance.setRotationDeg(req.rotationDeg());
        buildingInstanceRepo.save(instance);

        log.info("Moved building {} for civ={}", buildingInstanceId, civ.getId());
        return buildStateResponse(civ);
    }

    @Transactional(readOnly = true)
    public List<DecorationDefinitionDto> getDecorationDefinitions(UUID userId, String languageCode) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElse(null);
        String currentTier = civ != null ? civ.getCivilizationTier().name() : "VILLAGE";

        return decorationDefRepo.findByActiveTrueOrderByDisplayOrder().stream()
                .map(def -> new DecorationDefinitionDto(
                        def.getId(),
                        def.getDecorationType(),
                        def.getDisplayName(),
                        def.getDescription(),
                        def.getCoinCost(),
                        def.getWoodCost(),
                        def.getRequiredCivTier(),
                        def.isPremium(),
                        def.getWidthTiles(),
                        def.getHeightTiles()
                ))
                .toList();
    }

    @Transactional
    public CivilizationStateResponse placeDecoration(UUID userId, String languageCode, PlaceDecorationRequest req) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseThrow(() -> new CivilizationNotFoundException("Civilization not found for user=" + userId));

        LangoaDecorationDefinition def = decorationDefRepo.findAll().stream()
                .filter(d -> d.getDecorationType().equals(req.decorationType()))
                .findFirst()
                .orElseThrow(() -> new InsufficientResourcesException("Unknown decoration type: " + req.decorationType()));

        // Check tier requirement
        if (!isTierSufficient(civ.getCivilizationTier(), def.getRequiredCivTier())) {
            throw new InsufficientResourcesException(
                    def.getDecorationType() + " requires civilization tier: " + def.getRequiredCivTier());
        }

        long coins = getBalanceForUpdate(userId, code, CurrencyType.COINS);
        long wood  = getBalanceForUpdate(userId, code, CurrencyType.WOOD);

        if (coins < def.getCoinCost()) {
            throw new InsufficientResourcesException(
                    "Insufficient COINS: need " + def.getCoinCost() + ", have " + coins);
        }
        if (wood < def.getWoodCost()) {
            throw new InsufficientResourcesException(
                    "Insufficient WOOD: need " + def.getWoodCost() + ", have " + wood);
        }

        String decorRef = def.getDecorationType();
        if (def.getCoinCost() > 0)
            updateBalance(userId, code, CurrencyType.COINS, -def.getCoinCost(),
                    TransactionType.DECORATION_PURCHASE, decorRef, null);
        if (def.getWoodCost() > 0)
            updateBalance(userId, code, CurrencyType.WOOD, -def.getWoodCost(),
                    TransactionType.DECORATION_PURCHASE, decorRef, null);

        LangoaDecorationInstance instance = LangoaDecorationInstance.builder()
                .civilizationId(civ.getId())
                .decorationType(req.decorationType())
                .positionX(req.positionX())
                .positionY(req.positionY())
                .rotationDeg(req.rotationDeg())
                .build();
        decorationInstanceRepo.save(instance);

        log.info("Placed decoration {} for civ={}", req.decorationType(), civ.getId());
        return buildStateResponse(civ);
    }

    @Transactional(readOnly = true)
    public List<CityExpansionDto> getExpansions(UUID userId, String languageCode) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElse(null);

        if (civ == null) return List.of();

        Set<Integer> unlockedSlots = expansionInstanceRepo.findByCivilizationId(civ.getId())
                .stream().map(LangoaCityExpansionInstance::getExpansionSlot).collect(Collectors.toSet());

        Map<String, Long> balances = buildBalanceMap(userId, code);
        long coins = balances.getOrDefault(CurrencyType.COINS.name(), 0L);
        long wood  = balances.getOrDefault(CurrencyType.WOOD.name(), 0L);

        return expansionDefRepo.findAllByOrderByDisplayOrder().stream()
                .map(def -> {
                    boolean unlocked = unlockedSlots.contains(def.getExpansionSlot());
                    boolean canUnlock = !unlocked
                            && coins >= def.getCoinCost()
                            && wood >= def.getWoodCost()
                            && civ.getTotalLessonsCompleted() >= def.getRequiredLessons()
                            && civ.getTotalXp() >= def.getRequiredXp()
                            && isTierSufficient(civ.getCivilizationTier(), def.getRequiredCivTier());
                    return new CityExpansionDto(
                            def.getExpansionSlot(),
                            def.getDisplayName(),
                            def.getDescription(),
                            def.getGridXOffset(),
                            def.getGridYOffset(),
                            def.getGridWidth(),
                            def.getGridHeight(),
                            def.getCoinCost(),
                            def.getWoodCost(),
                            def.getRequiredLessons(),
                            def.getRequiredXp(),
                            def.getRequiredCivTier(),
                            unlocked,
                            canUnlock
                    );
                })
                .toList();
    }

    @Transactional
    public CityExpansionDto purchaseExpansion(UUID userId, String languageCode, int expansionSlot) {
        String code = resolveLanguageCode(languageCode);
        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseThrow(() -> new CivilizationNotFoundException("Civilization not found for user=" + userId));

        if (expansionInstanceRepo.existsByCivilizationIdAndExpansionSlot(civ.getId(), expansionSlot)) {
            throw new InsufficientResourcesException("Expansion slot " + expansionSlot + " already unlocked");
        }

        LangoaCityExpansionDefinition def = expansionDefRepo.findByExpansionSlot(expansionSlot)
                .orElseThrow(() -> new InsufficientResourcesException("Unknown expansion slot: " + expansionSlot));

        if (!isTierSufficient(civ.getCivilizationTier(), def.getRequiredCivTier())) {
            throw new InsufficientResourcesException("Requires civilization tier: " + def.getRequiredCivTier());
        }
        if (civ.getTotalLessonsCompleted() < def.getRequiredLessons()) {
            throw new InsufficientResourcesException("Requires " + def.getRequiredLessons() + " lessons completed");
        }
        if (civ.getTotalXp() < def.getRequiredXp()) {
            throw new InsufficientResourcesException("Requires " + def.getRequiredXp() + " total XP");
        }

        long coins = getBalanceForUpdate(userId, code, CurrencyType.COINS);
        long wood  = getBalanceForUpdate(userId, code, CurrencyType.WOOD);

        if (coins < def.getCoinCost()) {
            throw new InsufficientResourcesException("Insufficient COINS: need " + def.getCoinCost() + ", have " + coins);
        }
        if (wood < def.getWoodCost()) {
            throw new InsufficientResourcesException("Insufficient WOOD: need " + def.getWoodCost() + ", have " + wood);
        }

        String expRef = "expansion:" + expansionSlot;
        if (def.getCoinCost() > 0)
            updateBalance(userId, code, CurrencyType.COINS, -def.getCoinCost(),
                    TransactionType.EXPANSION_PURCHASE, expRef, null);
        if (def.getWoodCost() > 0)
            updateBalance(userId, code, CurrencyType.WOOD, -def.getWoodCost(),
                    TransactionType.EXPANSION_PURCHASE, expRef, null);

        LangoaCityExpansionInstance instance = LangoaCityExpansionInstance.builder()
                .civilizationId(civ.getId())
                .expansionSlot(expansionSlot)
                .build();
        try {
            expansionInstanceRepo.save(instance);
        } catch (DataIntegrityViolationException e) {
            throw new InsufficientResourcesException("Expansion slot " + expansionSlot + " already unlocked (concurrent)");
        }

        log.info("Expansion slot {} unlocked for civ={}", expansionSlot, civ.getId());
        return new CityExpansionDto(
                def.getExpansionSlot(), def.getDisplayName(), def.getDescription(),
                def.getGridXOffset(), def.getGridYOffset(), def.getGridWidth(), def.getGridHeight(),
                def.getCoinCost(), def.getWoodCost(), def.getRequiredLessons(), def.getRequiredXp(),
                def.getRequiredCivTier(), true, false
        );
    }

    // ── Package-visible methods used by QuestService / AchievementService ─────

    /** Package-visible so QuestService and AchievementService can grant rewards. */
    @Transactional
    public void updateBalancePublic(UUID userId, String languageCode, CurrencyType currencyType,
                                    long delta, TransactionType txType, String sourceRef, String idempotencyKey) {
        updateBalance(userId, languageCode, currencyType, delta, txType, sourceRef, idempotencyKey);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private LangoaCivilization createNewCivilization(UUID userId, String languageCode) {
        LangoaCivilization civ = LangoaCivilization.builder()
                .userId(userId)
                .languageCode(languageCode)
                .build();
        civ = civilizationRepo.save(civ);

        // Initialize all currency balance rows at 0
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
        updateBalance(userId, languageCode, CurrencyType.COINS,    200,
                TransactionType.INITIAL_GRANT, "new_civilization", null);
        updateBalance(userId, languageCode, CurrencyType.FOOD,      50,
                TransactionType.INITIAL_GRANT, "new_civilization", null);
        updateBalance(userId, languageCode, CurrencyType.MATERIALS,  30,
                TransactionType.INITIAL_GRANT, "new_civilization", null);
        updateBalance(userId, languageCode, CurrencyType.WOOD,       20,
                TransactionType.INITIAL_GRANT, "new_civilization", null);

        // Give starter HOUSE building for free at position (2,2)
        LangoaBuildingInstance house = LangoaBuildingInstance.builder()
                .civilizationId(civ.getId())
                .buildingType("HOUSE")
                .currentLevel(1)
                .positionX(2)
                .positionY(2)
                .buildState("BUILT")
                .build();
        buildingInstanceRepo.save(house);

        log.info("Created new civilization id={} for userId={} language={}", civ.getId(), userId, languageCode);
        return civ;
    }

    void updateBalance(UUID userId, String languageCode, CurrencyType currencyType,
                       long delta, TransactionType txType, String sourceRef, String idempotencyKey) {
        // SELECT FOR UPDATE prevents lost-update races when two transactions modify the same balance row
        LangoaCurrencyBalance bal = balanceRepo
                .findForUpdate(userId, languageCode, currencyType)
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

    private long getBalanceForUpdate(UUID userId, String languageCode, CurrencyType currencyType) {
        return balanceRepo.findForUpdate(userId, languageCode, currencyType)
                .map(LangoaCurrencyBalance::getBalance)
                .orElse(0L);
    }

    Map<String, Long> buildBalanceMap(UUID userId, String languageCode) {
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
                        inst.getPositionY(),
                        inst.getBuildState(),
                        inst.getRotationDeg(),
                        inst.getWidthTiles(),
                        inst.getHeightTiles()))
                .toList();

        // Decorations
        Map<String, String> decorDisplayNames = decorationDefRepo.findByActiveTrueOrderByDisplayOrder()
                .stream()
                .collect(Collectors.toMap(
                        LangoaDecorationDefinition::getDecorationType,
                        LangoaDecorationDefinition::getDisplayName));

        List<DecorationInstanceDto> decorations = decorationInstanceRepo.findByCivilizationId(civ.getId())
                .stream()
                .map(d -> new DecorationInstanceDto(
                        d.getId(),
                        d.getDecorationType(),
                        decorDisplayNames.getOrDefault(d.getDecorationType(), d.getDecorationType()),
                        d.getPositionX(),
                        d.getPositionY(),
                        d.getRotationDeg()))
                .toList();

        // Unlocked expansion slots
        List<Integer> unlockedSlots = expansionInstanceRepo.findByCivilizationId(civ.getId())
                .stream()
                .map(LangoaCityExpansionInstance::getExpansionSlot)
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
                buildings,
                decorations,
                unlockedSlots
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
        def.setWoodReward(5);
        def.setCivilizationPowerReward(100);
        return def;
    }

    private boolean isTierSufficient(CivilizationTier current, String requiredTierName) {
        try {
            CivilizationTier required = CivilizationTier.valueOf(requiredTierName);
            return current.ordinal() >= required.ordinal();
        } catch (IllegalArgumentException e) {
            return true; // unknown tier name — don't block
        }
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
        long wood      = balances.getOrDefault(CurrencyType.WOOD.name(),      0L);

        LangoaCivilization civ = civilizationRepo.findByUserIdAndLanguageCode(userId, code)
                .orElseGet(() -> createNewCivilization(userId, code));
        int totalLessons = civ.getTotalLessonsCompleted();
        long totalXp     = civ.getTotalXp();

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
                                        wood >= cfg.getWoodCost() &&
                                        totalLessons >= cfg.getRequiredLessonsCompleted() &&
                                        totalXp >= cfg.getRequiredXp();
                                return new BuildingLevelConfigDto(
                                        cfg.getLevel(),
                                        cfg.getDisplayName(),
                                        cfg.getCoinCost(),
                                        cfg.getFoodCost(),
                                        cfg.getMaterialCost(),
                                        cfg.getWoodCost(),
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
