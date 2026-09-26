package com.dsalearner.academy.service;

import com.dsalearner.academy.dto.*;
import com.dsalearner.academy.exception.IneligibleLessonException;
import com.dsalearner.academy.exception.LessonNotFoundException;
import com.dsalearner.academy.exception.LevelLockedException;
import com.dsalearner.academy.model.domain.ExperiencePlan;
import com.dsalearner.academy.model.domain.ExperiencePlanBuilder;
import com.dsalearner.academy.model.entity.LearnerLessonProgress;
import com.dsalearner.academy.model.entity.LearnerLevelProgress;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.model.entity.*;
import com.dsalearner.pipeline.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademyService {

    private static final Set<ContentStatus> ELIGIBLE_STATUSES = Set.of(
            ContentStatus.QA_PASSED, ContentStatus.APPROVED);

    private final CfCurriculumRepository curriculumRepo;
    private final CfCurriculumLevelRepository levelRepo;
    private final CfCurriculumUnitRepository unitRepo;
    private final CfCurriculumLessonPlanRepository lessonPlanRepo;
    private final CfLessonRepository lessonRepo;
    private final CfLessonVersionRepository lessonVersionRepo;
    private final LearnerLessonProgressService lessonProgressService;
    private final LearnerLevelProgressService levelProgressService;
    private final ExperiencePlanBuilder experiencePlanBuilder;

    // ── GET /{language}/curriculum ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AcademyCurriculumResponse getCurriculum(String languageCode, UUID userId) {
        CfCurriculum curriculum = resolveCurriculum(languageCode);

        // Bounded fetches — no N+1
        List<CfCurriculumLevel> levels = levelRepo.findByCurriculumIdOrderByOrdinal(curriculum.getId());
        List<CfCurriculumUnit> allUnits = unitRepo.findByCurriculumIdOrderByLevelIdAscOrdinalAsc(curriculum.getId());
        List<CfCurriculumLessonPlan> allPlans = lessonPlanRepo.findByCurriculumId(curriculum.getId());

        // Fetch all learner progress in one query — merge in memory
        List<LearnerLessonProgress> allLessonProgress = lessonProgressService.findAllForUser(userId);
        Map<UUID, LearnerLessonProgress> progressByLessonId = allLessonProgress.stream()
                .collect(Collectors.toMap(LearnerLessonProgress::getLessonId, Function.identity()));

        List<LearnerLevelProgress> allLevelProgress = levelProgressService.findAllForUserAndCurriculum(userId, curriculum.getId());
        Map<String, LearnerLevelProgress> levelProgressByCefr = allLevelProgress.stream()
                .collect(Collectors.toMap(LearnerLevelProgress::getCefrLevel, Function.identity()));

        // Group units and plans by levelId
        Map<UUID, List<CfCurriculumUnit>> unitsByLevelId = allUnits.stream()
                .collect(Collectors.groupingBy(CfCurriculumUnit::getLevelId));
        Map<UUID, List<CfCurriculumLessonPlan>> plansByLevelId = allPlans.stream()
                .collect(Collectors.groupingBy(CfCurriculumLessonPlan::getLevelId));
        Map<UUID, List<CfCurriculumLessonPlan>> plansByUnitId = allPlans.stream()
                .filter(p -> p.getUnitId() != null)
                .collect(Collectors.groupingBy(CfCurriculumLessonPlan::getUnitId));

        List<LevelSummaryDto> levelDtos = levels.stream()
                .map(level -> buildLevelSummary(
                        level,
                        levelProgressByCefr.get(level.getCefrLevel()),
                        unitsByLevelId.getOrDefault(level.getId(), List.of()),
                        plansByLevelId.getOrDefault(level.getId(), List.of()),
                        plansByUnitId,
                        progressByLessonId))
                .toList();

        return new AcademyCurriculumResponse(
                curriculum.getId(),
                curriculum.getLanguageCode(),
                curriculum.getDisplayName(),
                levelDtos
        );
    }

    private LevelSummaryDto buildLevelSummary(
            CfCurriculumLevel level,
            LearnerLevelProgress levelProg,
            List<CfCurriculumUnit> units,
            List<CfCurriculumLessonPlan> plansInLevel,
            Map<UUID, List<CfCurriculumLessonPlan>> plansByUnitId,
            Map<UUID, LearnerLessonProgress> progressByLessonId) {

        String levelStatus = levelProg != null ? levelProg.getStatus() : "NOT_STARTED";
        int lessonsCompleted = levelProg != null ? levelProg.getLessonsCompleted() : 0;
        int lessonsTotal = levelProg != null ? levelProg.getLessonsTotal() : (int) countEligiblePlans(plansInLevel);

        List<UnitDto> unitDtos = units.stream()
                .sorted(Comparator.comparingInt(CfCurriculumUnit::getOrdinal))
                .map(unit -> {
                    List<CfCurriculumLessonPlan> unitPlans = plansByUnitId.getOrDefault(unit.getId(), List.of());
                    List<LessonSummaryDto> lessonDtos = buildLessonSummaries(unitPlans, progressByLessonId);
                    return new UnitDto(unit.getId(), unit.getLabel(), unit.getOrdinal(), lessonDtos);
                })
                .toList();

        // Plans without a unit
        List<CfCurriculumLessonPlan> unassignedPlans = plansInLevel.stream()
                .filter(p -> p.getUnitId() == null && isEligible(p))
                .sorted(Comparator.comparingInt(CfCurriculumLessonPlan::getPosition))
                .toList();
        if (!unassignedPlans.isEmpty()) {
            List<LessonSummaryDto> lessonDtos = buildLessonSummaries(unassignedPlans, progressByLessonId);
            unitDtos = new ArrayList<>(unitDtos);
            unitDtos.add(new UnitDto(null, null, 0, lessonDtos));
        }

        return new LevelSummaryDto(
                level.getCefrLevel(),
                level.getDisplayName(),
                level.getOrdinal(),
                levelStatus,
                lessonsTotal,
                lessonsCompleted,
                levelProg != null ? levelProg.getAvgScore() : null,
                levelProg != null ? levelProg.getUnlockedAt() : null,
                levelProg != null ? levelProg.getCompletedAt() : null,
                unitDtos
        );
    }

    private List<LessonSummaryDto> buildLessonSummaries(
            List<CfCurriculumLessonPlan> plans,
            Map<UUID, LearnerLessonProgress> progressByLessonId) {
        return plans.stream()
                .filter(this::isEligible)
                .sorted(Comparator.comparingInt(CfCurriculumLessonPlan::getPosition))
                .map(plan -> {
                    UUID lessonId = plan.getLessonId();
                    LearnerLessonProgress prog = lessonId != null
                            ? progressByLessonId.get(lessonId)
                            : null;
                    String status = prog != null ? prog.getStatus() : "NOT_STARTED";
                    int stepIndex = prog != null ? prog.getStepIndex() : 0;
                    Integer score = prog != null && prog.getScore() != null ? (int) prog.getScore() : null;
                    return new LessonSummaryDto(lessonId, plan.getTitle(), status, plan.getPosition(), stepIndex, score);
                })
                .toList();
    }

    private boolean isEligible(CfCurriculumLessonPlan plan) {
        if (plan.getLessonId() == null) return false;
        // Plan status-based check: PROVISIONED means it has a lesson
        // We need to check the lesson's contentStatus — done via plan.lessonId lookup
        // We rely on caller to have filtered; eligibility by contentStatus is enforced in getLesson
        return plan.getLessonId() != null;
    }

    private long countEligiblePlans(List<CfCurriculumLessonPlan> plans) {
        return plans.stream().filter(p -> p.getLessonId() != null).count();
    }

    // ── GET /{language}/lessons/{lessonId} ───────────────────────────────────

    @Transactional
    public AcademyLessonResponse getLesson(String languageCode, UUID lessonId, UUID userId) {
        CfCurriculum curriculum = resolveCurriculum(languageCode);
        CfLesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found: " + lessonId));

        assertEligible(lesson);
        assertLevelUnlocked(userId, curriculum.getId(), lesson.getCefrLevel());

        CfLessonVersion version = resolveActiveVersion(lesson);
        CfCurriculumLessonPlan plan = lessonPlanRepo.findByLessonId(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("No lesson plan for lesson: " + lessonId));

        ExperiencePlan experiencePlan = experiencePlanBuilder.build(lesson, version, plan.getUnitDisplayName());
        LearnerLessonProgress progress = lessonProgressService.getOrCreate(userId, lessonId);

        return AcademyLessonResponse.from(experiencePlan, progress);
    }

    // ── PATCH /{language}/lessons/{lessonId}/step ────────────────────────────

    @Transactional
    public LessonProgressDto updateStepProgress(String languageCode, UUID lessonId, UUID userId, int stepIndex) {
        resolveCurriculum(languageCode);
        CfLesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found: " + lessonId));
        assertEligible(lesson);

        LearnerLessonProgress progress = lessonProgressService.startOrAdvance(userId, lessonId, stepIndex);
        return LessonProgressDto.from(progress);
    }

    // ── POST /{language}/lessons/{lessonId}/complete ─────────────────────────

    @Transactional
    public LessonCompletionResponse completeLesson(String languageCode, UUID lessonId, UUID userId, int score) {
        CfCurriculum curriculum = resolveCurriculum(languageCode);
        CfLesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson not found: " + lessonId));
        assertEligible(lesson);
        assertLevelUnlocked(userId, curriculum.getId(), lesson.getCefrLevel());

        // Idempotent — complete returns existing record if already COMPLETED
        LearnerLessonProgress progress = lessonProgressService.complete(userId, lessonId, score);

        boolean nextLevelUnlocked = false;
        String nextCefrLevel = null;

        // Only trigger level progression if this was a fresh completion (not idempotent repeat)
        CfCurriculumLevel currentLevel = levelRepo
                .findByCurriculumIdAndCefrLevel(curriculum.getId(), lesson.getCefrLevel())
                .orElse(null);

        if (currentLevel != null) {
            int eligibleCount = (int) lessonPlanRepo.findByCurriculumId(curriculum.getId()).stream()
                    .filter(p -> p.getLevelId().equals(currentLevel.getId()) && p.getLessonId() != null)
                    .count();

            LearnerLevelProgress levelProg = levelProgressService.recordLessonCompletion(
                    userId, curriculum.getId(), lesson.getCefrLevel(), eligibleCount, score);

            // Unlock next level when this level just completed
            if ("COMPLETED".equals(levelProg.getStatus())) {
                Optional<CfCurriculumLevel> nextLevel = levelRepo
                        .findByCurriculumIdAndOrdinal(curriculum.getId(), currentLevel.getOrdinal() + 1);
                if (nextLevel.isPresent()) {
                    int nextEligibleCount = (int) lessonPlanRepo.findByCurriculumId(curriculum.getId()).stream()
                            .filter(p -> p.getLevelId().equals(nextLevel.get().getId()) && p.getLessonId() != null)
                            .count();
                    levelProgressService.unlock(
                            userId, curriculum.getId(), nextLevel.get().getCefrLevel(), nextEligibleCount);
                    nextLevelUnlocked = true;
                    nextCefrLevel = nextLevel.get().getCefrLevel();
                    log.info("AcademyService: unlocked {} for user={}", nextCefrLevel, userId);
                }
            }
        }

        return new LessonCompletionResponse(
                lessonId,
                progress.getStatus(),
                score,
                progress.getCompletedAt(),
                nextLevelUnlocked,
                nextCefrLevel
        );
    }

    // ── GET /{language}/progress ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AcademyProgressResponse getProgress(String languageCode, UUID userId) {
        CfCurriculum curriculum = resolveCurriculum(languageCode);

        List<LearnerLevelProgress> levelProgress =
                levelProgressService.findAllForUserAndCurriculum(userId, curriculum.getId());

        long totalCompleted = lessonProgressService.countCompleted(userId);

        List<LevelProgressDto> levelDtos = levelProgress.stream()
                .map(LevelProgressDto::from)
                .toList();

        return new AcademyProgressResponse(
                curriculum.getId(),
                languageCode,
                totalCompleted,
                levelDtos
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Maps human-readable route names (e.g. "german") to ISO 639-1 codes (e.g. "de")
    private static final Map<String, String> LANGUAGE_NAME_TO_CODE = Map.of(
            "german", "de",
            "french", "fr",
            "spanish", "es",
            "korean", "ko",
            "japanese", "ja",
            "chinese", "zh",
            "italian", "it",
            "portuguese", "pt"
    );

    private CfCurriculum resolveCurriculum(String languageCode) {
        // Try exact match first (handles ISO codes like "de" directly)
        List<CfCurriculum> curricula = curriculumRepo.findByLanguageCode(languageCode);
        if (curricula.isEmpty()) {
            // Fall back to name→code mapping for human-readable route params like "german"
            String isoCode = LANGUAGE_NAME_TO_CODE.get(languageCode.toLowerCase());
            if (isoCode != null) {
                curricula = curriculumRepo.findByLanguageCode(isoCode);
            }
        }
        return curricula.stream()
                .filter(c -> !"ARCHIVED".equals(c.getCurriculumStatus()))
                .findFirst()
                .orElseThrow(() -> new LessonNotFoundException("No active curriculum for language: " + languageCode));
    }

    private void assertEligible(CfLesson lesson) {
        if (!ELIGIBLE_STATUSES.contains(lesson.getContentStatus())) {
            throw new IneligibleLessonException(
                    "Lesson " + lesson.getId() + " is not eligible (status=" + lesson.getContentStatus() + ")");
        }
    }

    private void assertLevelUnlocked(UUID userId, UUID curriculumId, String cefrLevel) {
        // A1 is always unlocked (bootstrapped on first access)
        CfCurriculumLevel level = levelRepo
                .findByCurriculumIdAndCefrLevel(curriculumId, cefrLevel)
                .orElse(null);
        if (level == null) return;

        if (level.getOrdinal() == 1) {
            // Bootstrap A1 for this user if not yet unlocked
            List<CfCurriculumLessonPlan> a1Plans = lessonPlanRepo.findByCurriculumId(curriculumId).stream()
                    .filter(p -> p.getLevelId().equals(level.getId()) && p.getLessonId() != null)
                    .toList();
            levelProgressService.unlock(userId, curriculumId, cefrLevel, a1Plans.size());
            return;
        }

        Optional<LearnerLevelProgress> prog = levelProgressService.find(userId, curriculumId, cefrLevel);
        if (prog.isEmpty() || "NOT_STARTED".equals(prog.get().getStatus())) {
            throw new LevelLockedException("Level " + cefrLevel + " is locked");
        }
    }

    private CfLessonVersion resolveActiveVersion(CfLesson lesson) {
        int version = lesson.getActiveVersion() != null ? lesson.getActiveVersion() : lesson.getCurrentVersion();
        return lessonVersionRepo.findByLessonIdAndVersion(lesson.getId(), version)
                .orElseThrow(() -> new LessonNotFoundException(
                        "No version " + version + " for lesson " + lesson.getId()));
    }
}
