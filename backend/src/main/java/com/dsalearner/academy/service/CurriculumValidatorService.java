package com.dsalearner.academy.service;

import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.PublicationStatus;
import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumRepository;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurriculumValidatorService {

    private static final Set<ContentStatus> ELIGIBLE_STATUSES = Set.of(
            ContentStatus.QA_PASSED, ContentStatus.APPROVED);

    private final CfCurriculumRepository curriculumRepo;
    private final CfCurriculumLessonPlanRepository lessonPlanRepo;
    private final CfLessonRepository lessonRepo;

    public record ValidationReport(
            String language,
            int totalPlans,
            int plansWithLesson,
            int publishedLessons,
            int eligibleLessons,
            List<String> issues,
            boolean passed
    ) {}

    @Transactional(readOnly = true)
    public ValidationReport validate(String languageCode) {
        List<String> issues = new ArrayList<>();

        List<CfCurriculum> curricula = curriculumRepo.findByLanguageCode(languageCode);
        if (curricula.isEmpty()) {
            return new ValidationReport(languageCode, 0, 0, 0, 0,
                    List.of("No curriculum found for language: " + languageCode), false);
        }

        CfCurriculum curriculum = curricula.stream()
                .filter(c -> !"ARCHIVED".equals(c.getCurriculumStatus()))
                .findFirst()
                .orElse(curricula.get(0));

        List<CfCurriculumLessonPlan> plans = lessonPlanRepo.findByCurriculumId(curriculum.getId());
        int totalPlans = plans.size();

        List<UUID> lessonIds = plans.stream()
                .map(CfCurriculumLessonPlan::getLessonId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        int plansWithLesson = (int) plans.stream()
                .filter(p -> p.getLessonId() != null).count();

        // Detect lesson plans missing a lesson
        long unlinked = plans.stream().filter(p -> p.getLessonId() == null).count();
        if (unlinked > 0) {
            issues.add(unlinked + " lesson plan(s) have no linked lesson");
        }

        // Detect duplicate stableRefs
        Map<UUID, CfLesson> lessonMap = lessonRepo.findAllById(lessonIds)
                .stream().collect(Collectors.toMap(CfLesson::getId, l -> l));

        Map<String, Long> refCounts = lessonMap.values().stream()
                .collect(Collectors.groupingBy(CfLesson::getStableRef, Collectors.counting()));
        refCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .forEach(e -> issues.add("Duplicate stableRef: " + e.getKey()));

        // Count eligible (approved/qa_passed) and published
        long eligibleCount = lessonMap.values().stream()
                .filter(l -> ELIGIBLE_STATUSES.contains(l.getContentStatus())).count();
        long publishedCount = lessonMap.values().stream()
                .filter(l -> PublicationStatus.PUBLISHED.equals(l.getPublicationStatus())).count();

        long ineligible = lessonMap.values().stream()
                .filter(l -> !ELIGIBLE_STATUSES.contains(l.getContentStatus())).count();
        if (ineligible > 0) {
            issues.add(ineligible + " lesson(s) are not in APPROVED/QA_PASSED status");
        }

        return new ValidationReport(
                languageCode,
                totalPlans,
                plansWithLesson,
                (int) publishedCount,
                (int) eligibleCount,
                issues,
                issues.isEmpty()
        );
    }
}
