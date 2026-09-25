package com.dsalearner.pipeline.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.DomainRegistry;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfWorkflowEvent;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.repository.CfWorkflowEventRepository;
import com.dsalearner.pipeline.statemachine.StateMachine;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import com.dsalearner.pipeline.validation.DeterministicValidator;
import com.dsalearner.pipeline.validation.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock CfLessonRepository lessonRepo;
    @Mock CfLessonVersionRepository versionRepo;
    @Mock CfWorkflowEventRepository eventRepo;
    @Mock WorkflowOrchestrator orchestrator;
    @Mock DeterministicValidator validator;
    @Mock DomainRegistry domainRegistry;

    private PipelineService service() {
        return new PipelineService(lessonRepo, versionRepo, eventRepo,
                orchestrator, validator, domainRegistry);
    }

    @Test
    void createLessonThrowsOnDuplicate() {
        when(lessonRepo.findByStableRef("de-a1-u01-l01")).thenReturn(Optional.of(mock(CfLesson.class)));
        assertThrows(ConflictException.class, () ->
                service().createLesson("de-a1-u01-l01", "language", "de", "A1", "Greetings", "system"));
    }

    /**
     * Blocker 4 regression: createLesson must succeed without going through the state machine.
     * The old code called orchestrator.applyContentTransition(lessonId, DRAFT, "CREATE", ...)
     * which the state machine rejected (DRAFT→DRAFT is not a valid transition).
     */
    @Test
    void createLessonSavesAndCreatesVersion() {
        when(lessonRepo.findByStableRef("de-a1-u01-l01")).thenReturn(Optional.empty());
        CfLesson saved = CfLesson.builder().id(UUID.randomUUID()).stableRef("de-a1-u01-l01")
                .domainCode("language").contentStatus(ContentStatus.DRAFT).currentVersion(1).build();
        when(lessonRepo.save(any())).thenReturn(saved);
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLesson result = service().createLesson("de-a1-u01-l01", "language", "de", "A1", "Greetings", "system");
        assertNotNull(result);
        verify(versionRepo).save(any());
        // Orchestrator must NOT be called for the CREATE event — it goes directly to eventRepo
        verify(orchestrator, never()).applyContentTransition(any(), eq(ContentStatus.DRAFT), any(), any(), any(), any());
    }

    /**
     * Blocker 4 regression: createLesson records a CREATE audit event directly (no state machine).
     * The event must have trigger=CREATE, toStatus=DRAFT, fromStatus=null, stableRef in metadata.
     */
    @Test
    void createLessonRecordsCreateAuditEventDirectly() {
        when(lessonRepo.findByStableRef("de-a1-u01-l01")).thenReturn(Optional.empty());
        UUID lessonId = UUID.randomUUID();
        CfLesson saved = CfLesson.builder().id(lessonId).stableRef("de-a1-u01-l01")
                .domainCode("language").contentStatus(ContentStatus.DRAFT).currentVersion(1).build();
        when(lessonRepo.save(any())).thenReturn(saved);
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().createLesson("de-a1-u01-l01", "language", "de", "A1", "Greetings", "system");

        ArgumentCaptor<CfWorkflowEvent> eventCaptor = ArgumentCaptor.forClass(CfWorkflowEvent.class);
        verify(eventRepo).save(eventCaptor.capture());
        CfWorkflowEvent event = eventCaptor.getValue();

        assertEquals("CREATE", event.getTrigger());
        assertEquals("DRAFT", event.getToStatus());
        assertNull(event.getFromStatus(), "CREATE event has no fromStatus — the lesson was just created");
        assertEquals("content", event.getStatusType());
        assertNotNull(event.getMetadata());
        assertEquals("de-a1-u01-l01", event.getMetadata().get("stableRef"));
    }

    /**
     * Blocker 4 regression: createLesson with status=DRAFT — no DRAFT→DRAFT transition through
     * the state machine. Verifying the state machine still rejects DRAFT→DRAFT when invoked directly.
     */
    @Test
    void stateMachineStillRejectsDraftToDraftTransition() {
        com.dsalearner.pipeline.statemachine.StateMachine sm = new com.dsalearner.pipeline.statemachine.StateMachine();
        assertThrows(com.dsalearner.pipeline.exception.InvalidTransitionException.class,
                () -> sm.transition(ContentStatus.DRAFT, ContentStatus.DRAFT, "CREATE"));
    }

    /**
     * Blocker 4 regression: PLAN transition (DRAFT→PLANNED) still works through orchestrator.
     */
    @Test
    void planTransitionGoesViaOrchestratorAsNormal() {
        UUID id = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder().id(id).domainCode("language")
                .contentStatus(ContentStatus.DRAFT).currentVersion(1).build();
        when(orchestrator.applyContentTransition(eq(id), eq(ContentStatus.PLANNED), any(), any(), any(), any()))
                .thenReturn(lesson);

        service().plan(id, "admin:user-1");
        verify(orchestrator).applyContentTransition(eq(id), eq(ContentStatus.PLANNED),
                eq("PLAN"), eq("admin:user-1"), isNull(), isNull());
    }

    @Test
    void getLessonThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(lessonRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service().getLesson(id));
    }

    @Test
    void submitForValidationPassRoutesToQaPending() {
        UUID id = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder().id(id).domainCode("language")
                .languageCode("de").contentStatus(ContentStatus.VALIDATION_PENDING)
                .currentVersion(1).build();
        when(lessonRepo.findById(id)).thenReturn(Optional.of(lesson));
        when(orchestrator.applyContentTransition(eq(id), eq(ContentStatus.VALIDATION_PENDING), any(), any(), any(), any()))
                .thenReturn(lesson);
        when(orchestrator.applyContentTransition(eq(id), eq(ContentStatus.QA_PENDING), any(), any(), any(), any()))
                .thenReturn(lesson);
        when(validator.validate(any(), eq("language"), eq("de"))).thenReturn(ValidationResult.pass());

        ValidationResult result = service().submitForValidation(id,
                Map.of("title", "t", "cefrLevel", "A1", "content", "c"), "system");
        assertTrue(result.passed());
        verify(orchestrator).applyContentTransition(eq(id), eq(ContentStatus.QA_PENDING), any(), any(), any(), any());
    }

    @Test
    void submitForValidationFailRoutesToValidationFailed() {
        UUID id = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder().id(id).domainCode("language")
                .languageCode("de").contentStatus(ContentStatus.VALIDATION_PENDING)
                .currentVersion(1).build();
        when(lessonRepo.findById(id)).thenReturn(Optional.of(lesson));
        when(orchestrator.applyContentTransition(eq(id), eq(ContentStatus.VALIDATION_PENDING), any(), any(), any(), any()))
                .thenReturn(lesson);
        when(orchestrator.applyContentTransition(eq(id), eq(ContentStatus.VALIDATION_FAILED), any(), any(), any(), any()))
                .thenReturn(lesson);
        when(validator.validate(any(), any(), any()))
                .thenReturn(ValidationResult.fail(java.util.List.of(
                        com.dsalearner.pipeline.validation.ValidationIssue.error("TEST", "field", "msg"))));

        ValidationResult result = service().submitForValidation(id, Map.of(), "system");
        assertFalse(result.passed());
        verify(orchestrator).applyContentTransition(eq(id), eq(ContentStatus.VALIDATION_FAILED), any(), any(), any(), any());
    }

    @Test
    void auditEventCreatedForEveryTransition() {
        UUID id = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder().id(id).domainCode("language")
                .contentStatus(ContentStatus.QA_PASSED).currentVersion(1).build();
        when(orchestrator.applyContentTransition(eq(id), eq(ContentStatus.APPROVED), any(), any(), any(), any()))
                .thenReturn(lesson);

        service().approve(id, "admin:user-1");
        // Orchestrator is responsible for audit; it must be called
        verify(orchestrator).applyContentTransition(eq(id), eq(ContentStatus.APPROVED),
                eq("APPROVE"), eq("admin:user-1"), isNull(), isNull());
    }
}
