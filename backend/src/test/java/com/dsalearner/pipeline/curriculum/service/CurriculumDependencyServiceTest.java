package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.pipeline.model.entity.CfCurriculumDependency;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.repository.CfCurriculumDependencyRepository;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumDependencyServiceTest {

    @Mock CfCurriculumDependencyRepository dependencyRepo;
    @Mock CfCurriculumLessonPlanRepository planRepo;

    private CurriculumDependencyService service() {
        return new CurriculumDependencyService(dependencyRepo, planRepo);
    }

    private static final UUID CURRICULUM_ID = UUID.randomUUID();

    // ─── record: self-dependency ─────────────────────────────────────────

    @Test
    void recordIgnoresSelfDependency() {
        UUID planId = UUID.randomUUID();
        CurriculumDependencyService svc = service();
        CfCurriculumDependency result = svc.record(planId, planId, "PREREQUISITE");
        assertThat(result).isNull();
        verifyNoInteractions(dependencyRepo);
    }

    // ─── record: idempotency ─────────────────────────────────────────────

    @Test
    void recordIsIdempotentForExistingDependency() {
        UUID planId = UUID.randomUUID();
        UUID reqId  = UUID.randomUUID();

        when(dependencyRepo.existsByLessonPlanIdAndRequiredPlanId(planId, reqId)).thenReturn(true);
        CfCurriculumDependency existing = CfCurriculumDependency.builder()
                .lessonPlanId(planId).requiredPlanId(reqId).dependencyType("PREREQUISITE").build();
        when(dependencyRepo.findByLessonPlanIdAndRequiredPlanId(planId, reqId))
                .thenReturn(Optional.of(existing));

        CfCurriculumDependency result = service().record(planId, reqId, "PREREQUISITE");
        assertThat(result).isEqualTo(existing);
        verify(dependencyRepo, never()).save(any());
    }

    // ─── record: new dependency ──────────────────────────────────────────

    @Test
    void recordSavesNewDependency() {
        UUID planId = UUID.randomUUID();
        UUID reqId  = UUID.randomUUID();

        when(dependencyRepo.existsByLessonPlanIdAndRequiredPlanId(planId, reqId)).thenReturn(false);
        when(dependencyRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfCurriculumDependency result = service().record(planId, reqId, "PREREQUISITE");

        assertThat(result.getLessonPlanId()).isEqualTo(planId);
        assertThat(result.getRequiredPlanId()).isEqualTo(reqId);
        assertThat(result.getDependencyType()).isEqualTo("PREREQUISITE");
        verify(dependencyRepo).save(any(CfCurriculumDependency.class));
    }

    @Test
    void recordDefaultsDependencyTypeToPrerequisite() {
        UUID planId = UUID.randomUUID();
        UUID reqId  = UUID.randomUUID();

        when(dependencyRepo.existsByLessonPlanIdAndRequiredPlanId(planId, reqId)).thenReturn(false);
        when(dependencyRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfCurriculumDependency result = service().record(planId, reqId, null);
        assertThat(result.getDependencyType()).isEqualTo("PREREQUISITE");
    }

    // ─── resolveUnblocked: empty when all blocked ────────────────────────

    @Test
    void resolveUnblockedReturnsEmptyWhenQueueEmpty() {
        UUID levelId = UUID.randomUUID();
        when(planRepo.findByLevelIdAndPlanStatusOrderByPosition(levelId, "QUEUED"))
                .thenReturn(List.of());

        List<CfCurriculumLessonPlan> result = service().resolveUnblocked(levelId);
        assertThat(result).isEmpty();
    }

    @Test
    void resolveUnblockedReturnsPlanWithNoDependencies() {
        UUID levelId = UUID.randomUUID();
        UUID planId  = UUID.randomUUID();
        CfCurriculumLessonPlan plan = plan(planId, "QUEUED");

        when(planRepo.findByLevelIdAndPlanStatusOrderByPosition(levelId, "QUEUED"))
                .thenReturn(List.of(plan));
        when(dependencyRepo.findByLessonPlanId(planId)).thenReturn(List.of());

        List<CfCurriculumLessonPlan> result = service().resolveUnblocked(levelId);
        assertThat(result).containsExactly(plan);
    }

    @Test
    void resolveUnblockedReturnsPlanWhenAllRequiredPassed() {
        UUID levelId  = UUID.randomUUID();
        UUID planId   = UUID.randomUUID();
        UUID reqId    = UUID.randomUUID();
        CfCurriculumLessonPlan plan    = plan(planId, "QUEUED");
        CfCurriculumLessonPlan reqPlan = plan(reqId, "QA_PASSED");

        when(planRepo.findByLevelIdAndPlanStatusOrderByPosition(levelId, "QUEUED"))
                .thenReturn(List.of(plan));
        CfCurriculumDependency dep = CfCurriculumDependency.builder()
                .lessonPlanId(planId).requiredPlanId(reqId).dependencyType("PREREQUISITE").build();
        when(dependencyRepo.findByLessonPlanId(planId)).thenReturn(List.of(dep));
        when(planRepo.findById(reqId)).thenReturn(Optional.of(reqPlan));

        List<CfCurriculumLessonPlan> result = service().resolveUnblocked(levelId);
        assertThat(result).containsExactly(plan);
    }

    @Test
    void resolveUnblockedExcludesPlanWhenRequiredNotPassed() {
        UUID levelId  = UUID.randomUUID();
        UUID planId   = UUID.randomUUID();
        UUID reqId    = UUID.randomUUID();
        CfCurriculumLessonPlan plan    = plan(planId, "QUEUED");
        CfCurriculumLessonPlan reqPlan = plan(reqId, "GENERATING"); // not yet passed

        when(planRepo.findByLevelIdAndPlanStatusOrderByPosition(levelId, "QUEUED"))
                .thenReturn(List.of(plan));
        CfCurriculumDependency dep = CfCurriculumDependency.builder()
                .lessonPlanId(planId).requiredPlanId(reqId).dependencyType("PREREQUISITE").build();
        when(dependencyRepo.findByLessonPlanId(planId)).thenReturn(List.of(dep));
        when(planRepo.findById(reqId)).thenReturn(Optional.of(reqPlan));

        List<CfCurriculumLessonPlan> result = service().resolveUnblocked(levelId);
        assertThat(result).isEmpty();
    }

    // ─── refreshBlockedStatus ────────────────────────────────────────────

    @Test
    void refreshSetsPlannedToQueuedWhenNoDeps() {
        UUID levelId = UUID.randomUUID();
        UUID planId  = UUID.randomUUID();
        CfCurriculumLessonPlan plan = plan(planId, "PLANNED");

        when(planRepo.findByLevelIdAndPlanStatusOrderByPosition(levelId, "PLANNED"))
                .thenReturn(List.of(plan));
        when(dependencyRepo.findByLessonPlanId(planId)).thenReturn(List.of());
        when(planRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().refreshBlockedStatus(levelId);
        assertThat(plan.getPlanStatus()).isEqualTo("QUEUED");
    }

    @Test
    void refreshSetsPlannedToBlockedWhenDepsUnmet() {
        UUID levelId  = UUID.randomUUID();
        UUID planId   = UUID.randomUUID();
        UUID reqId    = UUID.randomUUID();
        CfCurriculumLessonPlan plan    = plan(planId, "PLANNED");
        CfCurriculumLessonPlan reqPlan = plan(reqId, "PLANNED"); // not yet passed

        when(planRepo.findByLevelIdAndPlanStatusOrderByPosition(levelId, "PLANNED"))
                .thenReturn(List.of(plan));
        CfCurriculumDependency dep = CfCurriculumDependency.builder()
                .lessonPlanId(planId).requiredPlanId(reqId).dependencyType("PREREQUISITE").build();
        when(dependencyRepo.findByLessonPlanId(planId)).thenReturn(List.of(dep));
        when(planRepo.findById(reqId)).thenReturn(Optional.of(reqPlan));
        when(planRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().refreshBlockedStatus(levelId);
        assertThat(plan.getPlanStatus()).isEqualTo("BLOCKED");
    }

    // ─── validateNoCycles ────────────────────────────────────────────────

    @Test
    void validateNoCyclesPassesForLinearChain() {
        UUID planA = UUID.randomUUID();
        UUID planB = UUID.randomUUID();
        UUID planC = UUID.randomUUID();

        when(planRepo.findByCurriculumId(CURRICULUM_ID)).thenReturn(
                List.of(plan(planA, "QUEUED"), plan(planB, "QUEUED"), plan(planC, "QUEUED")));
        // A → B → C (linear, no cycle)
        when(dependencyRepo.findAllByLessonPlanIdIn(anyList())).thenReturn(List.of(
                dep(planB, planA),
                dep(planC, planB)
        ));

        assertThatNoException().isThrownBy(() -> service().validateNoCycles(CURRICULUM_ID));
    }

    @Test
    void validateNoCyclesDetectsCycle() {
        UUID planA = UUID.randomUUID();
        UUID planB = UUID.randomUUID();

        when(planRepo.findByCurriculumId(CURRICULUM_ID)).thenReturn(
                List.of(plan(planA, "QUEUED"), plan(planB, "QUEUED")));
        // A → B and B → A (cycle)
        when(dependencyRepo.findAllByLessonPlanIdIn(anyList())).thenReturn(List.of(
                dep(planB, planA),
                dep(planA, planB)
        ));

        assertThatThrownBy(() -> service().validateNoCycles(CURRICULUM_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cycle detected");
    }

    // ─── topologicalOrder ────────────────────────────────────────────────

    @Test
    void topologicalOrderReturnsNullOnCycle() {
        UUID planA = UUID.randomUUID();
        UUID planB = UUID.randomUUID();

        when(planRepo.findByCurriculumId(CURRICULUM_ID)).thenReturn(
                List.of(plan(planA, "QUEUED"), plan(planB, "QUEUED")));
        when(dependencyRepo.findAllByLessonPlanIdIn(anyList())).thenReturn(List.of(
                dep(planB, planA), dep(planA, planB)));

        List<UUID> order = service().topologicalOrder(CURRICULUM_ID);
        assertThat(order).isNull();
    }

    @Test
    void topologicalOrderCorrectForLinearChain() {
        UUID planA = UUID.randomUUID();
        UUID planB = UUID.randomUUID();

        when(planRepo.findByCurriculumId(CURRICULUM_ID)).thenReturn(
                List.of(plan(planA, "QUEUED"), plan(planB, "QUEUED")));
        // B depends on A: A comes before B
        when(dependencyRepo.findAllByLessonPlanIdIn(anyList())).thenReturn(List.of(dep(planB, planA)));

        List<UUID> order = service().topologicalOrder(CURRICULUM_ID);
        assertThat(order).containsExactly(planA, planB);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private CfCurriculumLessonPlan plan(UUID id, String status) {
        CfCurriculumLessonPlan p = CfCurriculumLessonPlan.builder()
                .stableRef("de-a1-u01-l01")
                .planStatus(status)
                .generationAttempt(0)
                .build();
        try {
            var f = CfCurriculumLessonPlan.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(p, id);
        } catch (Exception e) { throw new RuntimeException(e); }
        return p;
    }

    private CfCurriculumDependency dep(UUID planId, UUID requiredPlanId) {
        return CfCurriculumDependency.builder()
                .lessonPlanId(planId)
                .requiredPlanId(requiredPlanId)
                .dependencyType("PREREQUISITE")
                .build();
    }
}
