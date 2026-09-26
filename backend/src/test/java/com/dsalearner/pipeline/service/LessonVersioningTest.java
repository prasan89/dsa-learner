package com.dsalearner.pipeline.service;

import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.curriculum.service.CurriculumPublishingGateService;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Tests for lesson version immutability and content inheritance.
 *
 * Frozen = true means the version is immutable (published or superseded).
 * The service layer enforces this — the DB schema has no UPDATE trigger.
 *
 * createNextVersion copies content via deep-copy so v1 and v2 share no
 * mutable Map references, guaranteeing v1 is never modified via v2.
 */
@ExtendWith(MockitoExtension.class)
class LessonVersioningTest {

    @Mock CfLessonRepository lessonRepo;
    @Mock CfLessonVersionRepository versionRepo;
    @Mock com.dsalearner.pipeline.statemachine.WorkflowOrchestrator orchestrator;
    @Mock com.dsalearner.pipeline.validation.DeterministicValidator validator;
    @Mock com.dsalearner.pipeline.domain.DomainRegistry domainRegistry;
    @Mock com.dsalearner.pipeline.repository.CfWorkflowEventRepository eventRepo;
    @Mock CurriculumPublishingGateService publishingGateService;

    private PipelineService service() {
        return new PipelineService(lessonRepo, versionRepo, eventRepo,
                orchestrator, validator, domainRegistry, publishingGateService);
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

        CfLessonVersion result = service().updateVersionContent(
                lessonId, 1, Map.of("content", Map.of("title", "Updated")), "system");
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

    // ─── createNextVersion: content inheritance ───────────────────────────

    @Test
    void createNextVersionInheritsContentFromCurrentVersion() {
        UUID lessonId = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder()
                .id(lessonId).stableRef("de-a1-u01-l01").domainCode("language")
                .contentStatus(ContentStatus.APPROVED).currentVersion(1).build();

        CfLessonVersion v1 = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(true)
                .contentStatus("APPROVED")
                .blueprint(Map.of("unit", "1", "lesson", "1"))
                .content(Map.of("title", "Greetings", "body", "Hallo!"))
                .vocabulary(Map.of("words", "hallo,danke"))
                .grammar(Map.of("topic", "nominative"))
                .exercises(Map.of("count", "3"))
                .audioManifest(Map.of("files", "intro.mp3"))
                .promptVersions(Map.of("content_gen", "2"))
                .modelConfigs(Map.of("content_gen", "sonnet_gen_v1"))
                .generatorRunIds(Map.of("content_gen", "run-abc"))
                .revisionLog(Map.of("rev0", "initial"))
                .checksum("abc123")
                .build();

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(v1));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLessonVersion v2 = service().createNextVersion(lessonId, "system");

        assertEquals(2, v2.getVersion());
        assertFalse(v2.isFrozen());
        assertEquals(ContentStatus.REVISION.name(), v2.getContentStatus());

        // Content fields must be inherited
        assertEquals(v1.getBlueprint(), v2.getBlueprint());
        assertEquals(v1.getContent(), v2.getContent());
        assertEquals(v1.getVocabulary(), v2.getVocabulary());
        assertEquals(v1.getGrammar(), v2.getGrammar());
        assertEquals(v1.getExercises(), v2.getExercises());
        assertEquals(v1.getAudioManifest(), v2.getAudioManifest());

        // Lineage fields inherited for reviewer context
        assertEquals(v1.getPromptVersions(), v2.getPromptVersions());
        assertEquals(v1.getModelConfigs(), v2.getModelConfigs());
        assertEquals(v1.getGeneratorRunIds(), v2.getGeneratorRunIds());
        assertEquals(v1.getRevisionLog(), v2.getRevisionLog());
        assertEquals(v1.getChecksum(), v2.getChecksum());

        // qa_run_ids must NOT be inherited (v2 has not been QA'd)
        assertNull(v2.getQaRunIds());
    }

    @Test
    void createNextVersionDoesNotMutateSourceVersion() {
        UUID lessonId = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder()
                .id(lessonId).stableRef("de-a1-u01-l01").domainCode("language")
                .contentStatus(ContentStatus.APPROVED).currentVersion(1).build();

        // Use mutable HashMap so we can verify deep copy later
        Map<String, Object> originalContent = new HashMap<>();
        originalContent.put("title", "Original");

        CfLessonVersion v1 = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(true)
                .contentStatus("APPROVED")
                .content(originalContent)
                .build();

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(v1));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLessonVersion v2 = service().createNextVersion(lessonId, "system");

        // Mutate v2's content map
        v2.getContent().put("title", "Modified by v2");

        // v1's original content must be unchanged
        assertEquals("Original", v1.getContent().get("title"),
                "v1 content must not be modified when v2 is mutated — deep copy required");
    }

    @Test
    void createNextVersionV2StartsMutable() {
        UUID lessonId = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder()
                .id(lessonId).stableRef("de-a1-u01-l01").domainCode("language")
                .contentStatus(ContentStatus.APPROVED).currentVersion(1).build();

        CfLessonVersion v1 = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(true)
                .contentStatus("APPROVED")
                .content(Map.of("title", "Greetings"))
                .build();

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(v1));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLessonVersion v2 = service().createNextVersion(lessonId, "system");

        assertFalse(v2.isFrozen(), "New version must start unfrozen for revision");
        // v2 must be updatable via updateVersionContent
        when(versionRepo.findByLessonIdAndVersion(lessonId, 2)).thenReturn(Optional.of(v2));

        // Should not throw
        assertDoesNotThrow(() -> service().updateVersionContent(lessonId, 2,
                Map.of("content", Map.of("title", "Revised")), "system"));
    }

    @Test
    void createNextVersionQaRunIdsNotInherited() {
        UUID lessonId = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder()
                .id(lessonId).domainCode("language").currentVersion(1).build();

        CfLessonVersion v1 = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(true)
                .contentStatus("APPROVED")
                .qaRunIds(Map.of("cefr_qa", "run-qa-001"))  // v1 was QA'd
                .build();

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(v1));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLessonVersion v2 = service().createNextVersion(lessonId, "system");

        assertNull(v2.getQaRunIds(),
                "qa_run_ids must NOT be inherited: v2 has not been QA'd yet");
    }

    @Test
    void createNextVersionIncrementsCurrent() {
        UUID lessonId = UUID.randomUUID();
        CfLesson lesson = CfLesson.builder()
                .id(lessonId).stableRef("de-a1-u01-l01").domainCode("language")
                .contentStatus(ContentStatus.REVISION).currentVersion(1).build();

        CfLessonVersion v1 = CfLessonVersion.builder()
                .lessonId(lessonId).version(1).frozen(false)
                .contentStatus("REVISION").build();

        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(versionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(v1));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CfLessonVersion v2 = service().createNextVersion(lessonId, "system");
        assertEquals(2, v2.getVersion());

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
