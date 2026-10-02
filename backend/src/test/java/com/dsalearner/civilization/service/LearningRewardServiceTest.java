package com.dsalearner.civilization.service;

import com.dsalearner.civilization.model.entity.LangoaExerciseCompletion;
import com.dsalearner.civilization.model.entity.LangoaMilestoneReward;
import com.dsalearner.civilization.repository.LangoaExerciseCompletionRepository;
import com.dsalearner.civilization.repository.LangoaMilestoneRewardRepository;
import com.dsalearner.civilization.repository.LangoaTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LearningRewardServiceTest {

    @Mock CivilizationService civilizationService;
    @Mock LangoaMilestoneRewardRepository milestoneRepo;
    @Mock LangoaExerciseCompletionRepository exerciseCompletionRepo;
    @Mock LangoaTransactionRepository transactionRepo;

    @InjectMocks LearningRewardService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID lessonId = UUID.randomUUID();
    private final UUID curriculumId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(civilizationService.resolveLanguageCode(anyString())).thenReturn("de");
        lenient().when(transactionRepo.existsByIdempotencyKey(anyString())).thenReturn(false);
    }

    // ── Perfect lesson bonus ──────────────────────────────────────────────────

    @Test
    void grantPerfectLessonBonus_firstTime_creditsCoins() {
        when(milestoneRepo.findByMilestoneType("LESSON_PERFECT"))
                .thenReturn(Optional.of(new LangoaMilestoneReward("LESSON_PERFECT", 20L, "test")));
        doNothing().when(civilizationService)
                .updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());

        long result = service.grantPerfectLessonBonus(userId, "de", lessonId);

        assertThat(result).isEqualTo(20L);
        verify(civilizationService).updateBalancePublic(eq(userId), eq("de"), any(),
                eq(20L), any(), any(), contains("perfect-"));
    }

    @Test
    void grantPerfectLessonBonus_duplicate_dbConstraint_returnsZero() {
        when(milestoneRepo.findByMilestoneType("LESSON_PERFECT"))
                .thenReturn(Optional.of(new LangoaMilestoneReward("LESSON_PERFECT", 20L, "test")));
        doThrow(new DataIntegrityViolationException("duplicate idempotency_key"))
                .when(civilizationService)
                .updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());

        long result = service.grantPerfectLessonBonus(userId, "de", lessonId);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    void grantPerfectLessonBonus_missingConfig_returnsZero() {
        when(milestoneRepo.findByMilestoneType("LESSON_PERFECT")).thenReturn(Optional.empty());

        long result = service.grantPerfectLessonBonus(userId, "de", lessonId);

        assertThat(result).isEqualTo(0L);
        verify(civilizationService, never()).updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());
    }

    // ── Level completion bonus ────────────────────────────────────────────────

    @Test
    void grantLevelCompletionBonus_creditsConfiguredAmount() {
        when(milestoneRepo.findByMilestoneType("LEVEL_COMPLETED"))
                .thenReturn(Optional.of(new LangoaMilestoneReward("LEVEL_COMPLETED", 500L, "test")));
        doNothing().when(civilizationService)
                .updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());

        long result = service.grantLevelCompletionBonus(userId, "de", "A1", curriculumId);

        assertThat(result).isEqualTo(500L);
        verify(civilizationService).updateBalancePublic(eq(userId), eq("de"), any(),
                eq(500L), any(), any(), contains("level-A1-"));
    }

    @Test
    void grantLevelCompletionBonus_duplicate_returnsZero() {
        when(milestoneRepo.findByMilestoneType("LEVEL_COMPLETED"))
                .thenReturn(Optional.of(new LangoaMilestoneReward("LEVEL_COMPLETED", 500L, "test")));
        doThrow(new DataIntegrityViolationException("dup")).when(civilizationService)
                .updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());

        long result = service.grantLevelCompletionBonus(userId, "de", "A1", curriculumId);

        assertThat(result).isEqualTo(0L);
    }

    // ── Exercise reward ───────────────────────────────────────────────────────

    @Test
    void grantExerciseReward_firstTime_creditsCoins() {
        when(exerciseCompletionRepo.existsByUserIdAndExerciseIdAndLanguageCode(userId, "ex-1", "de"))
                .thenReturn(false);
        when(exerciseCompletionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(civilizationService)
                .updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());

        long result = service.grantExerciseReward(userId, lessonId, "ex-1", "de", 5L);

        assertThat(result).isEqualTo(5L);
        verify(exerciseCompletionRepo).save(any(LangoaExerciseCompletion.class));
        verify(civilizationService).updateBalancePublic(eq(userId), eq("de"), any(),
                eq(5L), any(), eq("ex-1"), contains("exercise-ex-1-"));
    }

    @Test
    void grantExerciseReward_duplicate_existsCheck_returnsZero() {
        when(exerciseCompletionRepo.existsByUserIdAndExerciseIdAndLanguageCode(userId, "ex-1", "de"))
                .thenReturn(true);

        long result = service.grantExerciseReward(userId, lessonId, "ex-1", "de", 5L);

        assertThat(result).isEqualTo(0L);
        verify(exerciseCompletionRepo, never()).save(any());
        verify(civilizationService, never()).updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void grantExerciseReward_concurrentDuplicate_dbConstraint_returnsZero() {
        when(exerciseCompletionRepo.existsByUserIdAndExerciseIdAndLanguageCode(userId, "ex-1", "de"))
                .thenReturn(false);
        when(exerciseCompletionRepo.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate uq_exercise_completion"));

        long result = service.grantExerciseReward(userId, lessonId, "ex-1", "de", 5L);

        assertThat(result).isEqualTo(0L);
        verify(civilizationService, never()).updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void grantExerciseReward_differentLanguages_treatedSeparately() {
        // German and Hindi exercises with same exerciseId are distinct
        when(exerciseCompletionRepo.existsByUserIdAndExerciseIdAndLanguageCode(userId, "ex-1", "de"))
                .thenReturn(true);
        when(exerciseCompletionRepo.existsByUserIdAndExerciseIdAndLanguageCode(userId, "ex-1", "hi"))
                .thenReturn(false);
        when(exerciseCompletionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(civilizationService.resolveLanguageCode("hi")).thenReturn("hi");
        doNothing().when(civilizationService)
                .updateBalancePublic(any(), any(), any(), anyLong(), any(), any(), any());

        long de = service.grantExerciseReward(userId, lessonId, "ex-1", "de", 5L);
        long hi = service.grantExerciseReward(userId, lessonId, "ex-1", "hi", 5L);

        assertThat(de).isEqualTo(0L);
        assertThat(hi).isEqualTo(5L);
    }
}
