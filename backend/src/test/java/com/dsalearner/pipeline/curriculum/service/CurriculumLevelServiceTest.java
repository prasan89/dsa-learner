package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumLevelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumLevelServiceTest {

    @Mock CfCurriculumLevelRepository levelRepo;
    @Mock CfCurriculumLessonPlanRepository planRepo;
    @Mock CurriculumWorkflowOrchestrator orchestrator;

    private CurriculumLevelService service() {
        return new CurriculumLevelService(levelRepo, planRepo, orchestrator);
    }

    private static final UUID CURRICULUM_ID = UUID.randomUUID();

    private CfCurriculumLevel level(UUID id, int ordinal, String status) {
        CfCurriculumLevel l = CfCurriculumLevel.builder()
                .curriculumId(CURRICULUM_ID)
                .cefrLevel("A" + ordinal)
                .ordinal(ordinal)
                .levelStatus(status)
                .build();
        // reflectively set ID so findById works in mocks
        try {
            var f = CfCurriculumLevel.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(l, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return l;
    }

    // ─── isEligibleToStart ────────────────────────────────────────────────

    @Test
    void firstLevelAlwaysEligible() {
        UUID levelId = UUID.randomUUID();
        when(levelRepo.findById(levelId)).thenReturn(Optional.of(level(levelId, 1, "PLANNED")));

        assertTrue(service().isEligibleToStart(levelId));
        verifyNoMoreInteractions(orchestrator);
    }

    @Test
    void secondLevelEligibleWhenPredecessorQaPassed() {
        UUID a1Id = UUID.randomUUID();
        UUID a2Id = UUID.randomUUID();
        CfCurriculumLevel a1 = level(a1Id, 1, "LEVEL_QA_PASSED");
        CfCurriculumLevel a2 = level(a2Id, 2, "PLANNED");

        when(levelRepo.findById(a2Id)).thenReturn(Optional.of(a2));
        when(levelRepo.findByCurriculumIdAndOrdinal(CURRICULUM_ID, 1)).thenReturn(Optional.of(a1));

        assertTrue(service().isEligibleToStart(a2Id));
    }

    @Test
    void secondLevelEligibleWhenPredecessorApproved() {
        UUID a1Id = UUID.randomUUID();
        UUID a2Id = UUID.randomUUID();
        CfCurriculumLevel a1 = level(a1Id, 1, "APPROVED");
        CfCurriculumLevel a2 = level(a2Id, 2, "PLANNED");

        when(levelRepo.findById(a2Id)).thenReturn(Optional.of(a2));
        when(levelRepo.findByCurriculumIdAndOrdinal(CURRICULUM_ID, 1)).thenReturn(Optional.of(a1));

        assertTrue(service().isEligibleToStart(a2Id));
    }

    @Test
    void secondLevelNotEligibleWhenPredecessorStillPlanned() {
        UUID a1Id = UUID.randomUUID();
        UUID a2Id = UUID.randomUUID();
        CfCurriculumLevel a1 = level(a1Id, 1, "PLANNED");
        CfCurriculumLevel a2 = level(a2Id, 2, "PLANNED");

        when(levelRepo.findById(a2Id)).thenReturn(Optional.of(a2));
        when(levelRepo.findByCurriculumIdAndOrdinal(CURRICULUM_ID, 1)).thenReturn(Optional.of(a1));

        assertFalse(service().isEligibleToStart(a2Id));
    }

    @Test
    void secondLevelNotEligibleWhenPredecessorGenerationInProgress() {
        UUID a1Id = UUID.randomUUID();
        UUID a2Id = UUID.randomUUID();
        CfCurriculumLevel a1 = level(a1Id, 1, "GENERATION_IN_PROGRESS");
        CfCurriculumLevel a2 = level(a2Id, 2, "PLANNED");

        when(levelRepo.findById(a2Id)).thenReturn(Optional.of(a2));
        when(levelRepo.findByCurriculumIdAndOrdinal(CURRICULUM_ID, 1)).thenReturn(Optional.of(a1));

        assertFalse(service().isEligibleToStart(a2Id));
    }

    // ─── startGeneration gate ─────────────────────────────────────────────

    @Test
    void startGenerationThrowsWhenGateClosed() {
        UUID a1Id = UUID.randomUUID();
        UUID a2Id = UUID.randomUUID();
        CfCurriculumLevel a1 = level(a1Id, 1, "PLANNED");
        CfCurriculumLevel a2 = level(a2Id, 2, "PLANNED");

        when(levelRepo.findById(a2Id)).thenReturn(Optional.of(a2));
        when(levelRepo.findByCurriculumIdAndOrdinal(CURRICULUM_ID, 1)).thenReturn(Optional.of(a1));

        assertThrows(ConflictException.class, () -> service().startGeneration(a2Id, "system"));
        verifyNoInteractions(orchestrator);
    }

    @Test
    void startGenerationDelegatesToOrchestratorWhenEligible() {
        UUID levelId = UUID.randomUUID();
        CfCurriculumLevel l = level(levelId, 1, "PLANNED");

        when(levelRepo.findById(levelId)).thenReturn(Optional.of(l));
        when(orchestrator.applyLevelTransition(eq(levelId), eq("GENERATION_IN_PROGRESS"),
                any(), any(), any(), any())).thenReturn(l);

        service().startGeneration(levelId, "system");
        verify(orchestrator).applyLevelTransition(eq(levelId), eq("GENERATION_IN_PROGRESS"),
                any(), any(), any(), any());
    }
}
