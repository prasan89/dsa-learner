package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Publishing gate for curriculum-linked lessons.
 * Standalone lessons (no row in cf_curriculum_lesson_plans) pass unrestricted.
 * Curriculum-linked lessons require the curriculum to be APPROVED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumPublishingGateService {

    private final CfCurriculumLessonPlanRepository lessonPlanRepository;
    private final CfCurriculumRepository curriculumRepository;

    /**
     * Throws {@link ConflictException} if the lesson is linked to a curriculum
     * whose publish gate has not yet been opened (i.e., curriculum is not APPROVED).
     * No-ops for standalone lessons.
     */
    public void assertPublishGateOpen(UUID lessonId) {
        Optional<CfCurriculumLessonPlan> planOpt = lessonPlanRepository.findByLessonId(lessonId);
        if (planOpt.isEmpty()) {
            // Standalone lesson — no gate
            return;
        }

        UUID curriculumId = planOpt.get().getCurriculumId();
        boolean gateOpen = curriculumRepository.findById(curriculumId)
                .map(c -> c.isPublishGatePassed())
                .orElse(true); // curriculum deleted — allow

        if (!gateOpen) {
            throw new ConflictException(
                    "Cannot publish lesson " + lessonId
                    + " — curriculum " + curriculumId + " publish gate is not yet open (curriculum must be APPROVED first)");
        }
    }

    public boolean isGateOpen(UUID lessonId) {
        return lessonPlanRepository.findByLessonId(lessonId)
                .map(plan -> curriculumRepository.findById(plan.getCurriculumId())
                        .map(c -> c.isPublishGatePassed())
                        .orElse(true))
                .orElse(true);
    }
}
