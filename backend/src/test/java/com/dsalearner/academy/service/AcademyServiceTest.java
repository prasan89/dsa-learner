package com.dsalearner.academy.service;

import com.dsalearner.academy.exception.IneligibleLessonException;
import com.dsalearner.academy.exception.LessonNotFoundException;
import com.dsalearner.academy.exception.LevelLockedException;
import com.dsalearner.academy.model.domain.ExperiencePlan;
import com.dsalearner.academy.model.domain.ExperiencePlanBuilder;
import com.dsalearner.academy.model.domain.ExperiencePlanStep;
import com.dsalearner.academy.model.domain.StepType;
import com.dsalearner.academy.model.entity.LearnerLessonProgress;
import com.dsalearner.academy.model.entity.LearnerLevelProgress;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.model.entity.*;
import com.dsalearner.pipeline.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademyServiceTest {

    @Mock CfCurriculumRepository curriculumRepo;
    @Mock CfCurriculumLevelRepository levelRepo;
    @Mock CfCurriculumUnitRepository unitRepo;
    @Mock CfCurriculumLessonPlanRepository lessonPlanRepo;
    @Mock CfLessonRepository lessonRepo;
    @Mock CfLessonVersionRepository lessonVersionRepo;
    @Mock LearnerLessonProgressService lessonProgressService;
    @Mock LearnerLevelProgressService levelProgressService;
    @Mock ExperiencePlanBuilder experiencePlanBuilder;

    @InjectMocks AcademyService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID curriculumId = UUID.randomUUID();
    private final UUID lessonId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();

    private CfCurriculum curriculum;
    private CfCurriculumLevel levelA1;
    private CfLesson lesson;
    private CfLessonVersion version;
    private CfCurriculumLessonPlan plan;

    @BeforeEach
    void setUp() {
        curriculum = CfCurriculum.builder()
                .id(curriculumId)
                .stableRef("de-lang")
                .languageCode("de")
                .displayName("German")
                .curriculumStatus("ACTIVE")
                .build();

        levelA1 = CfCurriculumLevel.builder()
                .id(UUID.randomUUID())
                .curriculumId(curriculumId)
                .cefrLevel("A1")
                .displayName("Beginner")
                .ordinal(1)
                .levelStatus("ACTIVE")
                .build();

        lesson = CfLesson.builder()
                .id(lessonId)
                .stableRef("de-a1-greetings")
                .languageCode("de")
                .cefrLevel("A1")
                .title("Greetings")
                .contentStatus(ContentStatus.QA_PASSED)
                .currentVersion(1)
                .build();

        version = CfLessonVersion.builder()
                .id(versionId)
                .lessonId(lessonId)
                .version(1)
                .contentStatus("QA_PASSED")
                .content(Map.of("sections", List.of(Map.of("text", "Hallo!"))))
                .vocabulary(Map.of("items", List.of()))
                .grammar(Map.of("rules", List.of()))
                .exercises(Map.of("items", List.of(
                        Map.of("type", "MULTIPLE_CHOICE", "question", "Q?",
                               "options", List.of("A", "B"), "correctIndex", 0))))
                .build();

        plan = CfCurriculumLessonPlan.builder()
                .id(UUID.randomUUID())
                .curriculumId(curriculumId)
                .levelId(levelA1.getId())
                .lessonId(lessonId)
                .title("Greetings")
                .stableRef("de-a1-greetings-plan")
                .position(1)
                .unitDisplayName("Unit 1: Basics")
                .planStatus("PROVISIONED")
                .build();
    }

    // ── getCurriculum ─────────────────────────────────────────────────────────

    @Test
    void getCurriculum_returnsResponse_withLevelsAndUnits() {
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(levelRepo.findByCurriculumIdOrderByOrdinal(curriculumId)).thenReturn(List.of(levelA1));
        when(unitRepo.findByCurriculumIdOrderByLevelIdAscOrdinalAsc(curriculumId)).thenReturn(List.of());
        when(lessonPlanRepo.findByCurriculumId(curriculumId)).thenReturn(List.of(plan));
        when(lessonProgressService.findAllForUser(userId)).thenReturn(List.of());
        when(levelProgressService.findAllForUserAndCurriculum(userId, curriculumId)).thenReturn(List.of());

        var response = service.getCurriculum("de", userId);

        assertThat(response.curriculumId()).isEqualTo(curriculumId);
        assertThat(response.languageCode()).isEqualTo("de");
        assertThat(response.levels()).hasSize(1);
        assertThat(response.levels().get(0).cefrLevel()).isEqualTo("A1");
    }

    @Test
    void getCurriculum_throwsWhenNoCurriculumForLanguage() {
        when(curriculumRepo.findByLanguageCode("fr")).thenReturn(List.of());

        assertThatThrownBy(() -> service.getCurriculum("fr", userId))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessageContaining("fr");
    }

    @Test
    void getCurriculum_skipsArchivedCurricula() {
        CfCurriculum archived = CfCurriculum.builder()
                .id(UUID.randomUUID()).languageCode("de").stableRef("old")
                .displayName("Old German").curriculumStatus("ARCHIVED").build();
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(archived));

        assertThatThrownBy(() -> service.getCurriculum("de", userId))
                .isInstanceOf(LessonNotFoundException.class);
    }

    // ── getLesson ─────────────────────────────────────────────────────────────

    @Test
    void getLesson_returnsLessonResponse() {
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(levelRepo.findByCurriculumIdAndCefrLevel(curriculumId, "A1")).thenReturn(Optional.of(levelA1));
        when(lessonPlanRepo.findByCurriculumId(curriculumId)).thenReturn(List.of(plan));
        when(levelProgressService.unlock(any(), any(), any(), anyInt()))
                .thenReturn(LearnerLevelProgress.builder().status("IN_PROGRESS").build());
        when(lessonVersionRepo.findByLessonIdAndVersion(lessonId, 1)).thenReturn(Optional.of(version));
        when(lessonPlanRepo.findByLessonId(lessonId)).thenReturn(Optional.of(plan));
        LearnerLessonProgress progress = LearnerLessonProgress.builder()
                .id(UUID.randomUUID()).userId(userId).lessonId(lessonId)
                .status("NOT_STARTED").stepIndex(0).build();
        when(lessonProgressService.getOrCreate(userId, lessonId)).thenReturn(progress);

        ExperiencePlan expPlan = new ExperiencePlan(lessonId, versionId, 1,
                "Greetings", "A1", "de", "Unit 1: Basics",
                List.of(
                        ExperiencePlanStep.of(0, StepType.NARRATIVE, Map.of("text", "Hallo!")),
                        ExperiencePlanStep.of(1, StepType.MULTIPLE_CHOICE,
                                Map.of("question", "Q?", "options", List.of("A", "B"), "correctIndex", 0)),
                        ExperiencePlanStep.of(2, StepType.LESSON_REVIEW, Map.of("exerciseCount", 1))
                ));
        when(experiencePlanBuilder.build(any(), any(), any())).thenReturn(expPlan);

        var response = service.getLesson("de", lessonId, userId);

        assertThat(response.lessonId()).isEqualTo(lessonId);
        assertThat(response.title()).isEqualTo("Greetings");
        assertThat(response.learnerStatus()).isEqualTo("NOT_STARTED");
        assertThat(response.experiencePlan()).isNotNull();
    }

    @Test
    void getLesson_throwsWhenLessonNotFound() {
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLesson("de", lessonId, userId))
                .isInstanceOf(LessonNotFoundException.class);
    }

    @Test
    void getLesson_throwsWhenLessonIneligible() {
        CfLesson draftLesson = CfLesson.builder()
                .id(lessonId).stableRef("x").languageCode("de").cefrLevel("A1")
                .contentStatus(ContentStatus.DRAFT).currentVersion(1).build();
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(draftLesson));

        assertThatThrownBy(() -> service.getLesson("de", lessonId, userId))
                .isInstanceOf(IneligibleLessonException.class);
    }

    @Test
    void getLesson_throwsWhenLevelLocked() {
        CfLesson b1Lesson = CfLesson.builder()
                .id(lessonId).stableRef("x").languageCode("de").cefrLevel("B1")
                .contentStatus(ContentStatus.QA_PASSED).currentVersion(1).build();
        CfCurriculumLevel levelB1 = CfCurriculumLevel.builder()
                .id(UUID.randomUUID()).curriculumId(curriculumId)
                .cefrLevel("B1").displayName("Intermediate").ordinal(3).build();

        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(b1Lesson));
        when(levelRepo.findByCurriculumIdAndCefrLevel(curriculumId, "B1")).thenReturn(Optional.of(levelB1));
        when(levelProgressService.find(userId, curriculumId, "B1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLesson("de", lessonId, userId))
                .isInstanceOf(LevelLockedException.class)
                .hasMessageContaining("B1");
    }

    // ── completeLesson ────────────────────────────────────────────────────────

    @Test
    void completeLesson_returnsCompletionResponse() {
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(levelRepo.findByCurriculumIdAndCefrLevel(curriculumId, "A1")).thenReturn(Optional.of(levelA1));
        when(lessonPlanRepo.findByCurriculumId(curriculumId)).thenReturn(List.of(plan));
        when(levelProgressService.unlock(any(), any(), any(), anyInt()))
                .thenReturn(LearnerLevelProgress.builder().status("IN_PROGRESS").build());
        LearnerLessonProgress completed = LearnerLessonProgress.builder()
                .id(UUID.randomUUID()).userId(userId).lessonId(lessonId)
                .status("COMPLETED").score((short) 85).build();
        when(lessonProgressService.complete(userId, lessonId, 85)).thenReturn(completed);
        LearnerLevelProgress levelProg = LearnerLevelProgress.builder()
                .userId(userId).curriculumId(curriculumId).cefrLevel("A1")
                .status("IN_PROGRESS").lessonsTotal(5).lessonsCompleted(3).build();
        when(levelProgressService.recordLessonCompletion(any(), any(), eq("A1"), anyInt(), eq(85)))
                .thenReturn(levelProg);

        var response = service.completeLesson("de", lessonId, userId, 85);

        assertThat(response.lessonId()).isEqualTo(lessonId);
        assertThat(response.score()).isEqualTo(85);
        assertThat(response.lessonStatus()).isEqualTo("COMPLETED");
        assertThat(response.nextLevelUnlocked()).isFalse();
    }

    @Test
    void completeLesson_unlocksNextLevelWhenCurrentCompletes() {
        CfCurriculumLevel levelA2 = CfCurriculumLevel.builder()
                .id(UUID.randomUUID()).curriculumId(curriculumId)
                .cefrLevel("A2").displayName("Elementary").ordinal(2).build();

        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(levelRepo.findByCurriculumIdAndCefrLevel(curriculumId, "A1")).thenReturn(Optional.of(levelA1));
        when(lessonPlanRepo.findByCurriculumId(curriculumId)).thenReturn(List.of(plan));
        when(levelProgressService.unlock(any(), any(), eq("A1"), anyInt()))
                .thenReturn(LearnerLevelProgress.builder().status("IN_PROGRESS").build());
        LearnerLessonProgress completed = LearnerLessonProgress.builder()
                .id(UUID.randomUUID()).userId(userId).lessonId(lessonId)
                .status("COMPLETED").score((short) 100).build();
        when(lessonProgressService.complete(userId, lessonId, 100)).thenReturn(completed);
        LearnerLevelProgress completedLevel = LearnerLevelProgress.builder()
                .userId(userId).curriculumId(curriculumId).cefrLevel("A1")
                .status("COMPLETED").lessonsTotal(1).lessonsCompleted(1).build();
        when(levelProgressService.recordLessonCompletion(any(), any(), eq("A1"), anyInt(), eq(100)))
                .thenReturn(completedLevel);
        when(levelRepo.findByCurriculumIdAndOrdinal(curriculumId, 2)).thenReturn(Optional.of(levelA2));
        when(levelProgressService.unlock(any(), any(), eq("A2"), anyInt()))
                .thenReturn(LearnerLevelProgress.builder().status("IN_PROGRESS").build());

        var response = service.completeLesson("de", lessonId, userId, 100);

        assertThat(response.nextLevelUnlocked()).isTrue();
        assertThat(response.nextCefrLevel()).isEqualTo("A2");
    }

    @Test
    void completeLesson_isIdempotent() {
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(levelRepo.findByCurriculumIdAndCefrLevel(curriculumId, "A1")).thenReturn(Optional.of(levelA1));
        when(lessonPlanRepo.findByCurriculumId(curriculumId)).thenReturn(List.of(plan));
        when(levelProgressService.unlock(any(), any(), any(), anyInt()))
                .thenReturn(LearnerLevelProgress.builder().status("IN_PROGRESS").build());
        LearnerLessonProgress alreadyCompleted = LearnerLessonProgress.builder()
                .id(UUID.randomUUID()).userId(userId).lessonId(lessonId)
                .status("COMPLETED").score((short) 90).build();
        // LearnerLessonProgressService.complete() is idempotent — returns existing record
        when(lessonProgressService.complete(userId, lessonId, 70)).thenReturn(alreadyCompleted);
        when(levelProgressService.recordLessonCompletion(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(LearnerLevelProgress.builder().status("IN_PROGRESS").build());

        var response = service.completeLesson("de", lessonId, userId, 70);
        assertThat(response.score()).isEqualTo(70); // score from request, not stored
    }

    // ── updateStepProgress ────────────────────────────────────────────────────

    @Test
    void updateStepProgress_returnsLessonProgressDto() {
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        when(lessonRepo.findById(lessonId)).thenReturn(Optional.of(lesson));
        LearnerLessonProgress progress = LearnerLessonProgress.builder()
                .id(UUID.randomUUID()).userId(userId).lessonId(lessonId)
                .status("IN_PROGRESS").stepIndex(3).build();
        when(lessonProgressService.startOrAdvance(userId, lessonId, 3)).thenReturn(progress);

        var result = service.updateStepProgress("de", lessonId, userId, 3);

        assertThat(result.stepIndex()).isEqualTo(3);
        assertThat(result.status()).isEqualTo("IN_PROGRESS");
    }

    // ── getProgress ───────────────────────────────────────────────────────────

    @Test
    void getProgress_returnsProgressSummary() {
        when(curriculumRepo.findByLanguageCode("de")).thenReturn(List.of(curriculum));
        LearnerLevelProgress levelProg = LearnerLevelProgress.builder()
                .userId(userId).curriculumId(curriculumId).cefrLevel("A1")
                .status("IN_PROGRESS").lessonsTotal(10).lessonsCompleted(3).build();
        when(levelProgressService.findAllForUserAndCurriculum(userId, curriculumId))
                .thenReturn(List.of(levelProg));
        when(lessonProgressService.countCompleted(userId)).thenReturn(3L);

        var response = service.getProgress("de", userId);

        assertThat(response.curriculumId()).isEqualTo(curriculumId);
        assertThat(response.totalLessonsCompleted()).isEqualTo(3L);
        assertThat(response.levels()).hasSize(1);
        assertThat(response.levels().get(0).cefrLevel()).isEqualTo("A1");
    }

    // ── Security: language is data ─────────────────────────────────────────────

    @Test
    void getCurriculum_routesCorrectlyByLanguageCode_noHardcodedLanguageChecks() {
        // Any language code must go through the same code path
        UUID frCurriculumId = UUID.randomUUID();
        CfCurriculum frCurriculum = CfCurriculum.builder()
                .id(frCurriculumId).stableRef("fr-lang").languageCode("fr")
                .displayName("French").curriculumStatus("ACTIVE").build();
        when(curriculumRepo.findByLanguageCode("fr")).thenReturn(List.of(frCurriculum));
        when(levelRepo.findByCurriculumIdOrderByOrdinal(frCurriculumId)).thenReturn(List.of());
        when(unitRepo.findByCurriculumIdOrderByLevelIdAscOrdinalAsc(frCurriculumId)).thenReturn(List.of());
        when(lessonPlanRepo.findByCurriculumId(frCurriculumId)).thenReturn(List.of());
        when(lessonProgressService.findAllForUser(userId)).thenReturn(List.of());
        when(levelProgressService.findAllForUserAndCurriculum(userId, frCurriculumId)).thenReturn(List.of());

        var response = service.getCurriculum("fr", userId);
        assertThat(response.languageCode()).isEqualTo("fr");
    }
}
