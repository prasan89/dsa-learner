package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CivilizationTier;
import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.dto.QuestDto;
import com.dsalearner.civilization.model.entity.*;
import com.dsalearner.civilization.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock LangoaQuestDefinitionRepository questDefRepo;
    @Mock LangoaQuestProgressRepository questProgressRepo;
    @Mock CivilizationService civilizationService;

    @InjectMocks QuestService questService;

    private final UUID userId = UUID.randomUUID();
    private final UUID questId = UUID.randomUUID();

    private LangoaQuestDefinition lessonQuest;

    @BeforeEach
    void setUp() {
        when(civilizationService.resolveLanguageCode("de")).thenReturn("de");

        lessonQuest = LangoaQuestDefinition.builder()
                .id(questId)
                .questKey("daily_lesson_1")
                .displayName("First Step")
                .questType("DAILY")
                .targetType("LESSONS_COMPLETED")
                .targetCount(1)
                .xpReward(100L)
                .coinReward(50L)
                .foodReward(10L)
                .materialReward(5L)
                .active(true)
                .build();
    }

    @Test
    void test_progressQuests_completesQuest_grantsReward() {
        when(questDefRepo.findByTargetTypeAndActiveTrue("LESSONS_COMPLETED"))
                .thenReturn(List.of(lessonQuest));
        when(questProgressRepo.findByUserIdAndLanguageCodeAndQuestIdAndQuestDate(
                eq(userId), eq("de"), eq(questId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(questProgressRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<String> completed = questService.progressQuests(userId, "de", "LESSONS_COMPLETED", 1);

        assertThat(completed).containsExactly("daily_lesson_1");
        verify(questProgressRepo, atLeast(2)).save(any(LangoaQuestProgress.class));
        // Reward should be applied
        verify(civilizationService).updateBalancePublic(eq(userId), eq("de"),
                eq(CurrencyType.XP), eq(100L), any(), any(), isNull());
        verify(civilizationService).updateBalancePublic(eq(userId), eq("de"),
                eq(CurrencyType.COINS), eq(50L), any(), any(), isNull());
    }

    @Test
    void test_progressQuests_alreadyCompleted_skipped() {
        LangoaQuestProgress completedProgress = LangoaQuestProgress.builder()
                .userId(userId).languageCode("de").questId(questId)
                .currentCount(1).completed(true).rewardClaimed(true)
                .build();

        when(questDefRepo.findByTargetTypeAndActiveTrue("LESSONS_COMPLETED"))
                .thenReturn(List.of(lessonQuest));
        when(questProgressRepo.findByUserIdAndLanguageCodeAndQuestIdAndQuestDate(
                eq(userId), eq("de"), eq(questId), any(LocalDate.class)))
                .thenReturn(Optional.of(completedProgress));

        List<String> completed = questService.progressQuests(userId, "de", "LESSONS_COMPLETED", 1);

        assertThat(completed).isEmpty();
        verify(questProgressRepo, never()).save(any());
        verify(civilizationService, never()).updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void test_progressQuests_partialProgress_notYetComplete() {
        LangoaQuestDefinition threeQuest = LangoaQuestDefinition.builder()
                .id(UUID.randomUUID())
                .questKey("daily_lesson_3")
                .questType("DAILY")
                .targetType("LESSONS_COMPLETED")
                .targetCount(3)
                .xpReward(250L).coinReward(120L).foodReward(0L).materialReward(0L)
                .active(true)
                .build();

        when(questDefRepo.findByTargetTypeAndActiveTrue("LESSONS_COMPLETED"))
                .thenReturn(List.of(threeQuest));
        when(questProgressRepo.findByUserIdAndLanguageCodeAndQuestIdAndQuestDate(
                eq(userId), eq("de"), eq(threeQuest.getId()), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(questProgressRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<String> completed = questService.progressQuests(userId, "de", "LESSONS_COMPLETED", 1);

        assertThat(completed).isEmpty(); // count=1, target=3 — not yet complete
        verify(questProgressRepo, atLeast(1)).save(any()); // create + update = 2 saves
        verify(civilizationService, never()).updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());
    }
}
