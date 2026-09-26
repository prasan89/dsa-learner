package com.dsalearner.academy.service;

import com.dsalearner.academy.model.entity.LearnerLevelProgress;
import com.dsalearner.academy.repository.LearnerLevelProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearnerLevelProgressServiceTest {

    @Mock
    private LearnerLevelProgressRepository repo;

    @InjectMocks
    private LearnerLevelProgressService service;

    private UUID userId;
    private UUID curriculumId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        curriculumId = UUID.randomUUID();
    }

    private LearnerLevelProgress notStarted(String cefr) {
        return LearnerLevelProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .curriculumId(curriculumId)
                .cefrLevel(cefr)
                .status("NOT_STARTED")
                .lessonsTotal(20)
                .lessonsCompleted(0)
                .build();
    }

    @Test
    void getOrCreate_returnsExistingRecord() {
        LearnerLevelProgress existing = notStarted("A1");
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.of(existing));

        LearnerLevelProgress result = service.getOrCreate(userId, curriculumId, "A1", 20);

        assertThat(result).isSameAs(existing);
        verify(repo, never()).save(any());
    }

    @Test
    void getOrCreate_createsRecordWhenAbsent() {
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLevelProgress result = service.getOrCreate(userId, curriculumId, "A1", 20);

        assertThat(result.getStatus()).isEqualTo("NOT_STARTED");
        assertThat(result.getLessonsTotal()).isEqualTo(20);
    }

    @Test
    void unlock_transitionsNotStartedToInProgress() {
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.of(notStarted("A1")));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLevelProgress result = service.unlock(userId, curriculumId, "A1", 20);

        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(result.getUnlockedAt()).isNotNull();
    }

    @Test
    void unlock_idempotentWhenAlreadyInProgress() {
        LearnerLevelProgress inProgress = notStarted("A1");
        inProgress.setStatus("IN_PROGRESS");
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.of(inProgress));

        LearnerLevelProgress result = service.unlock(userId, curriculumId, "A1", 20);

        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
        verify(repo, never()).save(any());
    }

    @Test
    void recordLessonCompletion_incrementsCompletedCount() {
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.of(notStarted("A1")));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLevelProgress result = service.recordLessonCompletion(userId, curriculumId, "A1", 20, 80);

        assertThat(result.getLessonsCompleted()).isEqualTo(1);
    }

    @Test
    void recordLessonCompletion_computesAverageScore() {
        LearnerLevelProgress existing = notStarted("A1");
        existing.setLessonsCompleted(1);
        existing.setAvgScore(new BigDecimal("80.00"));
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLevelProgress result = service.recordLessonCompletion(userId, curriculumId, "A1", 20, 60);

        // avg(80, 60) = 70
        assertThat(result.getAvgScore()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void recordLessonCompletion_triggersCompletedWhenAllLessonsDone() {
        LearnerLevelProgress existing = notStarted("A1");
        existing.setStatus("IN_PROGRESS");
        existing.setLessonsTotal(3);
        existing.setLessonsCompleted(2);
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLevelProgress result = service.recordLessonCompletion(userId, curriculumId, "A1", 3, 90);

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void recordLessonCompletion_doesNotExceedLessonsTotal() {
        LearnerLevelProgress existing = notStarted("A1");
        existing.setStatus("COMPLETED");
        existing.setLessonsTotal(3);
        existing.setLessonsCompleted(3);
        when(repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, "A1"))
                .thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LearnerLevelProgress result = service.recordLessonCompletion(userId, curriculumId, "A1", 3, 100);

        assertThat(result.getLessonsCompleted()).isEqualTo(3);
    }
}
