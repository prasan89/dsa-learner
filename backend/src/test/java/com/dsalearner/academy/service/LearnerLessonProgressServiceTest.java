package com.dsalearner.academy.service;

import com.dsalearner.academy.model.entity.LearnerLessonProgress;
import com.dsalearner.academy.repository.LearnerLessonProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearnerLessonProgressServiceTest {

    @Mock
    private LearnerLessonProgressRepository repo;

    @InjectMocks
    private LearnerLessonProgressService service;

    private UUID userId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
    }

    private LearnerLessonProgress notStarted() {
        return LearnerLessonProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .lessonId(lessonId)
                .status("NOT_STARTED")
                .stepIndex(0)
                .build();
    }

    @Test
    void getOrCreate_returnsExistingRecord() {
        LearnerLessonProgress existing = notStarted();
        when(repo.findByUserIdAndLessonId(userId, lessonId)).thenReturn(Optional.of(existing));

        LearnerLessonProgress result = service.getOrCreate(userId, lessonId);

        assertThat(result).isSameAs(existing);
        verify(repo, never()).save(any());
    }

    @Test
    void getOrCreate_createsNewRecordWhenAbsent() {
        when(repo.findByUserIdAndLessonId(userId, lessonId)).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLessonProgress result = service.getOrCreate(userId, lessonId);

        assertThat(result.getStatus()).isEqualTo("NOT_STARTED");
        assertThat(result.getStepIndex()).isEqualTo(0);
        verify(repo).save(any());
    }

    @Test
    void startOrAdvance_transitionsNotStartedToInProgress() {
        when(repo.findByUserIdAndLessonId(userId, lessonId)).thenReturn(Optional.of(notStarted()));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLessonProgress result = service.startOrAdvance(userId, lessonId, 1);

        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getStartedAt()).isNotNull();
        assertThat(result.getStepIndex()).isEqualTo(1);
    }

    @Test
    void startOrAdvance_doesNotResetStartedAtIfAlreadyInProgress() {
        Instant originalStart = Instant.now().minusSeconds(60);
        LearnerLessonProgress inProgress = LearnerLessonProgress.builder()
                .id(UUID.randomUUID()).userId(userId).lessonId(lessonId)
                .status("IN_PROGRESS").stepIndex(2).startedAt(originalStart).build();
        when(repo.findByUserIdAndLessonId(userId, lessonId)).thenReturn(Optional.of(inProgress));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLessonProgress result = service.startOrAdvance(userId, lessonId, 3);

        assertThat(result.getStartedAt()).isEqualTo(originalStart);
        assertThat(result.getStepIndex()).isEqualTo(3);
    }

    @Test
    void complete_setsStatusAndScore() {
        when(repo.findByUserIdAndLessonId(userId, lessonId)).thenReturn(Optional.of(notStarted()));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLessonProgress result = service.complete(userId, lessonId, 85);

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getScore()).isEqualTo((short) 85);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void complete_idempotentWhenAlreadyCompleted() {
        LearnerLessonProgress completed = LearnerLessonProgress.builder()
                .id(UUID.randomUUID()).userId(userId).lessonId(lessonId)
                .status("COMPLETED").stepIndex(5).score((short) 90).build();
        when(repo.findByUserIdAndLessonId(userId, lessonId)).thenReturn(Optional.of(completed));

        LearnerLessonProgress result = service.complete(userId, lessonId, 70);

        assertThat(result.getScore()).isEqualTo((short) 90);
        verify(repo, never()).save(any());
    }

    @Test
    void complete_rejectsScoreAbove100() {
        assertThatThrownBy(() -> service.complete(userId, lessonId, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("score must be 0–100");
    }

    @Test
    void complete_rejectsNegativeScore() {
        assertThatThrownBy(() -> service.complete(userId, lessonId, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAllForUser_delegatesToRepository() {
        when(repo.findByUserId(userId)).thenReturn(List.of(notStarted()));

        List<LearnerLessonProgress> results = service.findAllForUser(userId);

        assertThat(results).hasSize(1);
    }

    @Test
    void countCompleted_delegatesToRepository() {
        when(repo.countCompletedByUser(userId)).thenReturn(7L);

        assertThat(service.countCompleted(userId)).isEqualTo(7L);
    }
}
