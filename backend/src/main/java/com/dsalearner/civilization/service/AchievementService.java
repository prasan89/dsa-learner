package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.TransactionType;
import com.dsalearner.civilization.dto.AchievementDto;
import com.dsalearner.civilization.model.entity.*;
import com.dsalearner.civilization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final LangoaAchievementDefinitionRepository achievementDefRepo;
    private final LangoaAchievementProgressRepository achievementProgressRepo;
    private final CivilizationService civilizationService;

    @Transactional(readOnly = true)
    public List<AchievementDto> getAchievements(UUID userId, String languageCode) {
        String code = civilizationService.resolveLanguageCode(languageCode);

        List<LangoaAchievementDefinition> all = achievementDefRepo.findByActiveTrue();
        Map<UUID, LangoaAchievementProgress> progressMap = achievementProgressRepo
                .findByUserIdAndLanguageCode(userId, code)
                .stream()
                .collect(Collectors.toMap(LangoaAchievementProgress::getAchievementId, p -> p));

        return all.stream()
                .map(def -> {
                    LangoaAchievementProgress prog = progressMap.get(def.getId());
                    return new AchievementDto(
                            def.getId(),
                            def.getAchievementKey(),
                            def.getDisplayName(),
                            def.getDescription(),
                            def.getIcon(),
                            def.getTriggerValue(),
                            def.getXpReward(),
                            def.getCoinReward(),
                            prog != null && prog.isUnlocked()
                    );
                })
                .toList();
    }

    /**
     * Check achievements for a given trigger type and current cumulative value.
     * Returns keys of achievements newly unlocked.
     */
    @Transactional
    public List<String> checkAchievements(UUID userId, String languageCode, String triggerType, int currentValue) {
        String code = civilizationService.resolveLanguageCode(languageCode);

        List<LangoaAchievementDefinition> matching =
                achievementDefRepo.findByActiveTrueAndTriggerType(triggerType);

        Map<UUID, LangoaAchievementProgress> progressMap = achievementProgressRepo
                .findByUserIdAndLanguageCode(userId, code)
                .stream()
                .collect(Collectors.toMap(LangoaAchievementProgress::getAchievementId, p -> p));

        List<String> newlyUnlocked = new ArrayList<>();

        for (LangoaAchievementDefinition def : matching) {
            if (currentValue < def.getTriggerValue()) continue;

            LangoaAchievementProgress prog = progressMap.get(def.getId());
            if (prog != null && prog.isUnlocked()) continue; // already unlocked

            if (prog == null) {
                prog = LangoaAchievementProgress.builder()
                        .userId(userId)
                        .languageCode(code)
                        .achievementId(def.getId())
                        .build();
            }

            prog.setUnlocked(true);
            prog.setUnlockedAt(Instant.now());
            achievementProgressRepo.save(prog);

            // Grant achievement reward
            String ref = "achievement:" + def.getAchievementKey();
            if (def.getXpReward() > 0)
                civilizationService.updateBalancePublic(userId, code, CurrencyType.XP,
                        def.getXpReward(), TransactionType.ACHIEVEMENT_REWARD, ref, null);
            if (def.getCoinReward() > 0)
                civilizationService.updateBalancePublic(userId, code, CurrencyType.COINS,
                        def.getCoinReward(), TransactionType.ACHIEVEMENT_REWARD, ref, null);

            newlyUnlocked.add(def.getAchievementKey());
            log.info("Achievement {} unlocked for user={}", def.getAchievementKey(), userId);
        }

        return newlyUnlocked;
    }
}
