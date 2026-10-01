package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.dto.AchievementDto;
import com.dsalearner.civilization.model.entity.*;
import com.dsalearner.civilization.repository.*;
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
class AchievementServiceTest {

    @Mock LangoaAchievementDefinitionRepository achievementDefRepo;
    @Mock LangoaAchievementProgressRepository achievementProgressRepo;
    @Mock CivilizationService civilizationService;

    @InjectMocks AchievementService achievementService;

    private final UUID userId = UUID.randomUUID();
    private final UUID achId  = UUID.randomUUID();

    private LangoaAchievementDefinition firstLessonAch;

    @BeforeEach
    void setUp() {
        when(civilizationService.resolveLanguageCode("de")).thenReturn("de");

        firstLessonAch = LangoaAchievementDefinition.builder()
                .id(achId)
                .achievementKey("first_lesson")
                .displayName("First Step")
                .triggerType("LESSONS_COMPLETED")
                .triggerValue(1)
                .xpReward(200L)
                .coinReward(100L)
                .active(true)
                .build();
    }

    @Test
    void test_checkAchievements_unlocks_whenThresholdMet() {
        when(achievementDefRepo.findByActiveTrueAndTriggerType("LESSONS_COMPLETED"))
                .thenReturn(List.of(firstLessonAch));
        when(achievementProgressRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(List.of());
        when(achievementProgressRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<String> unlocked = achievementService.checkAchievements(userId, "de", "LESSONS_COMPLETED", 1);

        assertThat(unlocked).containsExactly("first_lesson");
        verify(achievementProgressRepo).save(argThat(p -> p.isUnlocked()));
        verify(civilizationService).updateBalancePublic(eq(userId), eq("de"),
                eq(CurrencyType.XP), eq(200L), any(), any(), isNull());
        verify(civilizationService).updateBalancePublic(eq(userId), eq("de"),
                eq(CurrencyType.COINS), eq(100L), any(), any(), isNull());
    }

    @Test
    void test_checkAchievements_noDuplicate_whenAlreadyUnlocked() {
        LangoaAchievementProgress existing = LangoaAchievementProgress.builder()
                .userId(userId).languageCode("de").achievementId(achId).unlocked(true).build();

        when(achievementDefRepo.findByActiveTrueAndTriggerType("LESSONS_COMPLETED"))
                .thenReturn(List.of(firstLessonAch));
        when(achievementProgressRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(List.of(existing));

        List<String> unlocked = achievementService.checkAchievements(userId, "de", "LESSONS_COMPLETED", 5);

        assertThat(unlocked).isEmpty();
        verify(achievementProgressRepo, never()).save(any());
        verify(civilizationService, never()).updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void test_checkAchievements_belowThreshold_noUnlock() {
        LangoaAchievementDefinition tenLessonsAch = LangoaAchievementDefinition.builder()
                .id(UUID.randomUUID())
                .achievementKey("lessons_10")
                .triggerType("LESSONS_COMPLETED")
                .triggerValue(10)
                .xpReward(500L).coinReward(250L)
                .active(true)
                .build();

        when(achievementDefRepo.findByActiveTrueAndTriggerType("LESSONS_COMPLETED"))
                .thenReturn(List.of(tenLessonsAch));
        when(achievementProgressRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(List.of());

        List<String> unlocked = achievementService.checkAchievements(userId, "de", "LESSONS_COMPLETED", 5);

        assertThat(unlocked).isEmpty();
        verify(achievementProgressRepo, never()).save(any());
    }

    @Test
    void test_getAchievements_showsUnlockStatus() {
        LangoaAchievementProgress progress = LangoaAchievementProgress.builder()
                .userId(userId).languageCode("de").achievementId(achId).unlocked(true).build();

        when(achievementDefRepo.findByActiveTrue()).thenReturn(List.of(firstLessonAch));
        when(achievementProgressRepo.findByUserIdAndLanguageCode(userId, "de"))
                .thenReturn(List.of(progress));

        List<AchievementDto> achievements = achievementService.getAchievements(userId, "de");

        assertThat(achievements).hasSize(1);
        assertThat(achievements.get(0).unlocked()).isTrue();
        assertThat(achievements.get(0).achievementKey()).isEqualTo("first_lesson");
    }
}
