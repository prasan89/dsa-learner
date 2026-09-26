package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.curriculum.agent.*;
import com.dsalearner.pipeline.model.entity.*;
import com.dsalearner.pipeline.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {

    @Mock CfCurriculumRepository curriculumRepo;
    @Mock CfCurriculumLevelRepository levelRepo;
    @Mock CfCurriculumUnitRepository unitRepo;
    @Mock CfCurriculumLessonPlanRepository planRepo;
    @Mock CfCurriculumVersionRepository versionRepo;
    @Mock CurriculumWorkflowOrchestrator orchestrator;

    private CurriculumService service() {
        return new CurriculumService(
                curriculumRepo, levelRepo, unitRepo, planRepo, versionRepo,
                orchestrator, new ObjectMapper());
    }

    private static final UUID CURRICULUM_ID = UUID.randomUUID();
    private static final UUID LEVEL_ID      = UUID.randomUUID();

    // ─── getById ──────────────────────────────────────────────────────────

    @Test
    void getByIdThrowsWhenAbsent() {
        when(curriculumRepo.findById(CURRICULUM_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().getById(CURRICULUM_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdReturnsEntity() {
        CfCurriculum c = curriculum();
        when(curriculumRepo.findById(CURRICULUM_ID)).thenReturn(Optional.of(c));
        assertThat(service().getById(CURRICULUM_ID)).isEqualTo(c);
    }

    // ─── Blueprint persistence ────────────────────────────────────────────

    @Test
    void persistBlueprintAssignsVersionNumberOne() {
        CfCurriculum c = curriculum();
        when(curriculumRepo.findById(CURRICULUM_ID)).thenReturn(Optional.of(c));
        when(versionRepo.findFirstByCurriculumIdOrderByVersionDesc(CURRICULUM_ID))
                .thenReturn(Optional.empty()); // no prior versions

        // Level row for A1
        CfCurriculumLevel level = level(LEVEL_ID, "A1");
        when(levelRepo.findByCurriculumIdAndCefrLevel(CURRICULUM_ID, "A1"))
                .thenReturn(Optional.of(level));
        when(planRepo.findByLevelIdOrderByPosition(LEVEL_ID)).thenReturn(List.of()); // no plans yet
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(unitRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(planRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(curriculumRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CurriculumBlueprint blueprint = minimalBlueprint();
        CfCurriculumVersion version = service().persistBlueprint(CURRICULUM_ID, blueprint, "initial");

        ArgumentCaptor<CfCurriculumVersion> versionCaptor = ArgumentCaptor.forClass(CfCurriculumVersion.class);
        verify(versionRepo).save(versionCaptor.capture());
        assertThat(versionCaptor.getValue().getVersion()).isEqualTo(1);
        assertThat(versionCaptor.getValue().isFrozen()).isTrue();
    }

    @Test
    void persistBlueprintIncrementsVersionNumber() {
        CfCurriculum c = curriculum();
        when(curriculumRepo.findById(CURRICULUM_ID)).thenReturn(Optional.of(c));
        CfCurriculumVersion prior = CfCurriculumVersion.builder()
                .curriculumId(CURRICULUM_ID).version(3).frozen(true).build();
        when(versionRepo.findFirstByCurriculumIdOrderByVersionDesc(CURRICULUM_ID))
                .thenReturn(Optional.of(prior));

        CfCurriculumLevel level = level(LEVEL_ID, "A1");
        when(levelRepo.findByCurriculumIdAndCefrLevel(CURRICULUM_ID, "A1"))
                .thenReturn(Optional.of(level));
        when(planRepo.findByLevelIdOrderByPosition(LEVEL_ID)).thenReturn(List.of());
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(unitRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(planRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(curriculumRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().persistBlueprint(CURRICULUM_ID, minimalBlueprint(), "update");

        ArgumentCaptor<CfCurriculumVersion> cap = ArgumentCaptor.forClass(CfCurriculumVersion.class);
        verify(versionRepo).save(cap.capture());
        assertThat(cap.getValue().getVersion()).isEqualTo(4);
    }

    @Test
    void persistBlueprintSkipsExpansionWhenPlansAlreadyExist() {
        CfCurriculum c = curriculum();
        when(curriculumRepo.findById(CURRICULUM_ID)).thenReturn(Optional.of(c));
        when(versionRepo.findFirstByCurriculumIdOrderByVersionDesc(CURRICULUM_ID))
                .thenReturn(Optional.empty());

        CfCurriculumLevel level = level(LEVEL_ID, "A1");
        when(levelRepo.findByCurriculumIdAndCefrLevel(CURRICULUM_ID, "A1"))
                .thenReturn(Optional.of(level));
        // Simulate existing plans (idempotency path)
        when(planRepo.findByLevelIdOrderByPosition(LEVEL_ID))
                .thenReturn(List.of(CfCurriculumLessonPlan.builder().stableRef("de-a1-u01-l01").build()));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(curriculumRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().persistBlueprint(CURRICULUM_ID, minimalBlueprint(), "idempotent");

        // Units and plans must NOT be saved — expansion was skipped
        verify(unitRepo, never()).save(any());
        verify(planRepo, never()).save(any());
    }

    @Test
    void persistBlueprintThrowsWhenLevelRowMissing() {
        CfCurriculum c = curriculum();
        when(curriculumRepo.findById(CURRICULUM_ID)).thenReturn(Optional.of(c));
        when(versionRepo.findFirstByCurriculumIdOrderByVersionDesc(CURRICULUM_ID))
                .thenReturn(Optional.empty());
        when(levelRepo.findByCurriculumIdAndCefrLevel(CURRICULUM_ID, "A1"))
                .thenReturn(Optional.empty()); // missing level row
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() ->
                service().persistBlueprint(CURRICULUM_ID, minimalBlueprint(), "failing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Level row missing");
    }

    // ─── provisionLesson ──────────────────────────────────────────────────

    @Test
    void provisionLessonSetsPlanStatusToGenerating() {
        UUID planId   = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        CfCurriculumLessonPlan plan = CfCurriculumLessonPlan.builder()
                .stableRef("de-a1-u01-l01")
                .planStatus("QUEUED")
                .generationAttempt(0)
                .build();
        when(planRepo.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfCurriculumLessonPlan result = service().provisionLesson(planId, lessonId);
        assertThat(result.getPlanStatus()).isEqualTo("GENERATING");
        assertThat(result.getLessonId()).isEqualTo(lessonId);
        assertThat(result.getGenerationAttempt()).isEqualTo(1);
    }

    @Test
    void provisionLessonIdempotentForSameLesson() {
        UUID planId   = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        CfCurriculumLessonPlan plan = CfCurriculumLessonPlan.builder()
                .stableRef("de-a1-u01-l01")
                .lessonId(lessonId)
                .planStatus("GENERATING")
                .generationAttempt(1)
                .build();
        when(planRepo.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Same lessonId → no conflict
        assertThatNoException().isThrownBy(() -> service().provisionLesson(planId, lessonId));
    }

    @Test
    void provisionLessonThrowsOnConflictingLesson() {
        UUID planId      = UUID.randomUUID();
        UUID lessonId    = UUID.randomUUID();
        UUID otherId     = UUID.randomUUID();
        CfCurriculumLessonPlan plan = CfCurriculumLessonPlan.builder()
                .stableRef("de-a1-u01-l01")
                .lessonId(otherId)  // different lesson already linked
                .planStatus("GENERATING")
                .generationAttempt(1)
                .build();
        when(planRepo.findById(planId)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service().provisionLesson(planId, lessonId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already provisioned");
    }

    // ─── isLevelGenerationComplete ────────────────────────────────────────

    @Test
    void levelCompleteWhenAllPlansQaPassed() {
        when(planRepo.countByLevelId(LEVEL_ID)).thenReturn(5L);
        when(planRepo.countByLevelIdAndPlanStatusIn(eq(LEVEL_ID), anyList())).thenReturn(5L);
        assertThat(service().isLevelGenerationComplete(LEVEL_ID)).isTrue();
    }

    @Test
    void levelNotCompleteWhenSomePlansPending() {
        when(planRepo.countByLevelId(LEVEL_ID)).thenReturn(5L);
        when(planRepo.countByLevelIdAndPlanStatusIn(eq(LEVEL_ID), anyList())).thenReturn(3L);
        assertThat(service().isLevelGenerationComplete(LEVEL_ID)).isFalse();
    }

    @Test
    void levelNotCompleteWhenNoPlanExist() {
        when(planRepo.countByLevelId(LEVEL_ID)).thenReturn(0L);
        assertThat(service().isLevelGenerationComplete(LEVEL_ID)).isFalse();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private CfCurriculum curriculum() {
        return CfCurriculum.builder()
                .stableRef("german-complete-v1")
                .domainCode("language")
                .languageCode("de")
                .displayName("German Complete")
                .curriculumStatus("DRAFT")
                .batchSize(10)
                .build();
    }

    private CfCurriculumLevel level(UUID id, String cefrLevel) {
        CfCurriculumLevel l = CfCurriculumLevel.builder()
                .curriculumId(CURRICULUM_ID)
                .cefrLevel(cefrLevel)
                .displayName("German " + cefrLevel)
                .ordinal(1)
                .levelStatus("PLANNED")
                .build();
        try {
            var f = CfCurriculumLevel.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(l, id);
        } catch (Exception e) { throw new RuntimeException(e); }
        return l;
    }

    private CurriculumBlueprint minimalBlueprint() {
        LessonPlanSlot slot = new LessonPlanSlot(
                1, "de-a1-u01-l01", "Greetings", "Hallo und Tschüss",
                "LEARN", "FOUNDATION",
                List.of("speaking"), "Introduce yourself",
                "Say hello and goodbye", List.of("nominative_case"),
                List.of("Hallo", "Tschüss"), List.of(), List.of());
        UnitBlueprint unit = new UnitBlueprint(1, "Unit 1", "Greetings", "Basic greetings", List.of(slot));
        LevelBlueprint level = new LevelBlueprint("A1", "German A1", "Foundation", List.of(unit));
        return new CurriculumBlueprint("German Complete", "de", List.of(level));
    }
}
