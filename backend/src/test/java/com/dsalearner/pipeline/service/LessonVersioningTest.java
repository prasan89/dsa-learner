package com.dsalearner.pipeline.service;

import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.exception.FrozenVersionException;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for lesson version immutability contract.
 *
 * Frozen = true means the version is immutable (published or superseded).
 * The service layer must enforce this — the DB schema has no UPDATE trigger,
 * so the application layer is the protection boundary.
 */
@ExtendWith(MockitoExtension.class)
class LessonVersioningTest {

    @Mock CfLessonRepository lessonRepo;
    @Mock CfLessonVersionRepository versionRepo;
    @Mock com.dsalearner.pipeline.statemachine.WorkflowOrchestrator orchestrator;
    @Mock com.dsalearner.pipeline.validation.DeterministicValidator validator;
    @Mock com.dsalearner.pipeline.domain.DomainRegistry domainRegistry;
    @Mock com.dsalearner.pipeline.repository.CfWorkflowEventRepository eventRepo;

    private PipelineService service() {
        return new PipelineService(lessonRepo, versionRepo, eventRepo,
                orchestrator, validator, domainRegistry);
    }

    // ─── Freeze ───────────────────────────────────────────────────────────

    @Test
    void canFreezeUnfrozenVersion() {
        UUID lessonId = UUID.randomUUID();
        CfLessonVersion unfrozen = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(false)
                .contentStatus("APPROVED").build();

        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(unfrozen));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLessonVersion result = service().freezeVersion(lessonId, 1, "admin:test");

        assertTrue(result.isFrozen());
        verify(versionRepo).save(argThat(CfLessonVersion::isFrozen));
    }

    @Test
    void freezingAlreadyFrozenVersionThrows() {
        UUID lessonId = UUID.randomUUID();
        CfLessonVersion frozen = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(true)
                .contentStatus("APPROVED").build();

        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(frozen));

        assertThrows(FrozenVersionException.class,
                () -> service().freezeVersion(lessonId, 1, "admin:test"));
        verify(versionRepo, never()).save(any());
    }

    // ─── Update content ───────────────────────────────────────────────────

    @Test
    void canUpdateUnfrozenVersionContent() {
        UUID lessonId = UUID.randomUUID();
        CfLessonVersion unfrozen = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(false)
                .contentStatus("REVISION").build();

        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(unfrozen));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> newContent = Map.of(
                "content", Map.of("title", "Updated lesson content"));

        CfLessonVersion result = service().updateVersionContent(lessonId, 1, newContent, "system");

        assertNotNull(result);
        verify(versionRepo).save(any());
    }

    @Test
    void updateFrozenVersionThrows() {
        UUID lessonId = UUID.randomUUID();
        CfLessonVersion frozen = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(true)
                .contentStatus("APPROVED").build();

        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(frozen));

        assertThrows(FrozenVersionException.class,
                () -> service().updateVersionContent(lessonId, 1,
                        Map.of("content", Map.of("title", "tampered")), "system"));

        verify(versionRepo, never()).save(any());
    }

    // ─── New version creation ─────────────────────────────────────────────

    @Test
    void createNextVersionIncrementsCurrent() {
        UUID lessonId = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder()
                .id(lessonId).stableRef("de-a1-u01-l01").domainCode("language")
                .contentStatus(ContentStatus.REVISION).currentVersion(1).build();

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLessonVersion v2 = service().createNextVersion(lessonId, "system");

        assertEquals(2, v2.getVersion());
        assertFalse(v2.isFrozen());
        assertEquals(ContentStatus.REVISION.name(), v2.getContentStatus());

        // lesson.currentVersion must be bumped
        ArgumentCaptor<CfLesson> lessonCaptor = ArgumentCaptor.forClass(CfLesson.class);
        verify(lessonRepo).save(lessonCaptor.capture());
        assertEquals(2, lessonCaptor.getValue().getCurrentVersion());
    }

    @Test
    void freezeThrowsWhenVersionNotFound() {
        UUID lessonId = UUID.randomUUID();
        when(versionRepo.findByLessonIdAndVersion(lessonId, 99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service().freezeVersion(lessonId, 99, "admin:test"));
    }
}
