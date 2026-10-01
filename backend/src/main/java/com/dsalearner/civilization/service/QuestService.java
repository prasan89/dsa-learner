package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.TransactionType;
import com.dsalearner.civilization.dto.QuestDto;
import com.dsalearner.civilization.model.entity.*;
import com.dsalearner.civilization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestService {

    private final LangoaQuestDefinitionRepository questDefRepo;
    private final LangoaQuestProgressRepository questProgressRepo;
    private final CivilizationService civilizationService;

    @Transactional(readOnly = true)
    public List<QuestDto> getActiveQuests(UUID userId, String languageCode) {
        String code = civilizationService.resolveLanguageCode(languageCode);
        LocalDate today = LocalDate.now();

        List<LangoaQuestDefinition> active = questDefRepo
                .findByActiveTrueAndQuestTypeIn(List.of("DAILY", "WEEKLY", "ONE_TIME"));

        Map<UUID, LangoaQuestProgress> progressMap = questProgressRepo
                .findByUserIdAndLanguageCodeAndQuestDate(userId, code, today)
                .stream()
                .collect(Collectors.toMap(LangoaQuestProgress::getQuestId, p -> p));

        return active.stream()
                .filter(def -> {
                    // ONE_TIME quests: show if not yet completed; DAILY/WEEKLY: always show
                    if ("ONE_TIME".equals(def.getQuestType())) {
                        LangoaQuestProgress prog = progressMap.get(def.getId());
                        return prog == null || !prog.isCompleted();
                    }
                    return true;
                })
                .map(def -> {
                    LangoaQuestProgress prog = progressMap.get(def.getId());
                    return new QuestDto(
                            def.getId(),
                            def.getQuestKey(),
                            def.getDisplayName(),
                            def.getDescription(),
                            def.getQuestType(),
                            def.getTargetCount(),
                            prog != null ? prog.getCurrentCount() : 0,
                            prog != null && prog.isCompleted(),
                            prog != null && prog.isRewardClaimed(),
                            def.getXpReward(),
                            def.getCoinReward(),
                            def.getFoodReward(),
                            def.getMaterialReward()
                    );
                })
                .toList();
    }

    /**
     * Progress all matching quests for the given trigger type.
     * Returns keys of quests that newly completed in this call.
     */
    @Transactional
    public List<String> progressQuests(UUID userId, String languageCode, String triggerType, int increment) {
        String code = civilizationService.resolveLanguageCode(languageCode);
        LocalDate today = LocalDate.now();

        List<LangoaQuestDefinition> matching = questDefRepo.findByTargetTypeAndActiveTrue(triggerType);
        List<String> newlyCompleted = new ArrayList<>();

        for (LangoaQuestDefinition def : matching) {
            LangoaQuestProgress progress = questProgressRepo
                    .findByUserIdAndLanguageCodeAndQuestIdAndQuestDate(userId, code, def.getId(), today)
                    .orElseGet(() -> questProgressRepo.save(LangoaQuestProgress.builder()
                            .userId(userId)
                            .languageCode(code)
                            .questId(def.getId())
                            .questDate(today)
                            .currentCount(0)
                            .build()));

            if (progress.isCompleted()) continue;

            progress.setCurrentCount(progress.getCurrentCount() + increment);
            if (progress.getCurrentCount() >= def.getTargetCount()) {
                progress.setCurrentCount(def.getTargetCount());
                progress.setCompleted(true);
                progress.setCompletedAt(Instant.now());
                newlyCompleted.add(def.getQuestKey());
                log.info("Quest {} completed for user={}", def.getQuestKey(), userId);

                // Auto-claim reward immediately
                if (!progress.isRewardClaimed()) {
                    progress.setRewardClaimed(true);
                    applyQuestReward(userId, code, def);
                }
            }
            questProgressRepo.save(progress);
        }

        return newlyCompleted;
    }

    private void applyQuestReward(UUID userId, String code, LangoaQuestDefinition def) {
        String ref = "quest:" + def.getQuestKey();
        if (def.getXpReward() > 0)
            civilizationService.updateBalancePublic(userId, code, CurrencyType.XP,
                    def.getXpReward(), TransactionType.QUEST_REWARD, ref, null);
        if (def.getCoinReward() > 0)
            civilizationService.updateBalancePublic(userId, code, CurrencyType.COINS,
                    def.getCoinReward(), TransactionType.QUEST_REWARD, ref, null);
        if (def.getFoodReward() > 0)
            civilizationService.updateBalancePublic(userId, code, CurrencyType.FOOD,
                    def.getFoodReward(), TransactionType.QUEST_REWARD, ref, null);
        if (def.getMaterialReward() > 0)
            civilizationService.updateBalancePublic(userId, code, CurrencyType.MATERIALS,
                    def.getMaterialReward(), TransactionType.QUEST_REWARD, ref, null);
    }
}
