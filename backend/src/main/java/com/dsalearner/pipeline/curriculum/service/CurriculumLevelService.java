package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Enforces level progression gates (ordinal N cannot start until ordinal N-1 is approved/qa-passed).
 * Also coordinates level status transitions when generation completes or QA runs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumLevelService {

    private final CfCurriculumLevelRepository levelRepository;
    private final CfCurriculumLessonPlanRepository lessonPlanRepository;
    private final CurriculumWorkflowOrchestrator orchestrator;

    /**
     * Returns true if the level is eligible to begin generation.
     * A1 (ordinal=1) is always eligible. Subsequent levels require the predecessor to be LEVEL_QA_PASSED or APPROVED.
     */
    public boolean isEligibleToStart(UUID levelId) {
        CfCurriculumLevel level = getLevel(levelId);
        if (level.getOrdinal() <= 1) {
            return true;
        }
        return levelRepository
                .findByCurriculumIdAndOrdinal(level.getCurriculumId(), level.getOrdinal() - 1)
                .map(prev -> "LEVEL_QA_PASSED".equals(prev.getLevelStatus())
                        || "APPROVED".equals(prev.getLevelStatus()))
                .orElse(false);
    }

    /**
     * Transitions a level to GENERATION_IN_PROGRESS, enforcing the gate.
     */
    @Transactional
    public CfCurriculumLevel startGeneration(UUID levelId, String actor) {
        CfCurriculumLevel level = getLevel(levelId);
        if (!isEligibleToStart(levelId)) {
            throw new ConflictException("Level " + level.getCefrLevel()
                    + " cannot start — predecessor level not yet approved");
        }
        return orchestrator.applyLevelTransition(levelId, "GENERATION_IN_PROGRESS",
                "batch_scheduler", actor, null, null);
    }

    /**
     * Transitions a level to LEVEL_QA_PENDING when all its plans are done.
     */
    @Transactional
    public CfCurriculumLevel submitForLevelQa(UUID levelId, UUID qaAgentRunId, String actor) {
        CfCurriculumLevel level = orchestrator.applyLevelTransition(
                levelId, "LEVEL_QA_PENDING", "level_qa_trigger", actor, qaAgentRunId, null);
        level.setLevelQaRunId(qaAgentRunId);
        return levelRepository.save(level);
    }

    /**
     * Applies QA result (PASSED or FAILED) to a level.
     */
    @Transactional
    public CfCurriculumLevel applyQaResult(UUID levelId, boolean passed, UUID qaAgentRunId, String actor) {
        String toStatus = passed ? "LEVEL_QA_PASSED" : "LEVEL_QA_FAILED";
        return orchestrator.applyLevelTransition(levelId, toStatus, "level_qa_result", actor, qaAgentRunId, null);
    }

    /**
     * Returns all PLANNED levels for a curriculum that are eligible to begin (predecessor gate open).
     */
    public List<CfCurriculumLevel> findEligibleLevels(UUID curriculumId) {
        return levelRepository.findByCurriculumIdOrderByOrdinal(curriculumId).stream()
                .filter(l -> "PLANNED".equals(l.getLevelStatus()))
                .filter(l -> isEligibleToStart(l.getId()))
                .toList();
    }

    /**
     * Returns true when every level in the curriculum is LEVEL_QA_PASSED or APPROVED.
     */
    public boolean isCurriculumGenerationComplete(UUID curriculumId) {
        List<CfCurriculumLevel> levels = levelRepository.findByCurriculumIdOrderByOrdinal(curriculumId);
        if (levels.isEmpty()) return false;
        return levels.stream().allMatch(l ->
                "LEVEL_QA_PASSED".equals(l.getLevelStatus()) || "APPROVED".equals(l.getLevelStatus()));
    }

    private CfCurriculumLevel getLevel(UUID levelId) {
        return levelRepository.findById(levelId)
                .orElseThrow(() -> new NotFoundException("Level not found: " + levelId));
    }
}
