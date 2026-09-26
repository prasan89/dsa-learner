package com.dsalearner.academy.service;

import com.dsalearner.academy.model.entity.LearnerLevelProgress;
import com.dsalearner.academy.repository.LearnerLevelProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerLevelProgressService {

    private final LearnerLevelProgressRepository repo;

    /**
     * Ensures a level-progress record exists. Creates one in NOT_STARTED if missing.
     */
    @Transactional
    public LearnerLevelProgress getOrCreate(UUID userId, UUID curriculumId, String cefrLevel, int lessonsTotal) {
        return repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, cefrLevel)
                .orElseGet(() -> {
                    LearnerLevelProgress p = LearnerLevelProgress.builder()
                            .userId(userId)
                            .curriculumId(curriculumId)
                            .cefrLevel(cefrLevel)
                            .status("NOT_STARTED")
                            .lessonsTotal(lessonsTotal)
                            .build();
                    return repo.save(p);
                });
    }

    @Transactional(readOnly = true)
    public Optional<LearnerLevelProgress> find(UUID userId, UUID curriculumId, String cefrLevel) {
        return repo.findByUserIdAndCurriculumIdAndCefrLevel(userId, curriculumId, cefrLevel);
    }

    @Transactional(readOnly = true)
    public List<LearnerLevelProgress> findAllForUserAndCurriculum(UUID userId, UUID curriculumId) {
        return repo.findByUserIdAndCurriculumIdOrderByCefrLevel(userId, curriculumId);
    }

    /**
     * Unlocks a level — transitions NOT_STARTED → IN_PROGRESS.
     * Called when the previous level completes or when the user is on A1 (first level).
     */
    @Transactional
    public LearnerLevelProgress unlock(UUID userId, UUID curriculumId, String cefrLevel, int lessonsTotal) {
        LearnerLevelProgress progress = getOrCreate(userId, curriculumId, cefrLevel, lessonsTotal);
        if (!"NOT_STARTED".equals(progress.getStatus())) return progress;
        progress.setStatus("IN_PROGRESS");
        progress.setUnlockedAt(Instant.now());
        LearnerLevelProgress saved = repo.save(progress);
        log.info("LearnerLevelProgressService: unlocked {}/{}/{}", userId, curriculumId, cefrLevel);
        return saved;
    }

    /**
     * Records a lesson completion event for this level.
     * Increments lessonsCompleted, recomputes avgScore, and may trigger COMPLETED status.
     */
    @Transactional
    public LearnerLevelProgress recordLessonCompletion(
            UUID userId, UUID curriculumId, String cefrLevel,
            int lessonsTotal, int newScore) {

        LearnerLevelProgress progress = getOrCreate(userId, curriculumId, cefrLevel, lessonsTotal);

        int prev = progress.getLessonsCompleted();
        int completed = prev + 1;
        progress.setLessonsCompleted(Math.min(completed, progress.getLessonsTotal()));

        // Running average of score
        BigDecimal prevAvg = progress.getAvgScore() != null ? progress.getAvgScore() : BigDecimal.ZERO;
        BigDecimal newAvg = prevAvg.multiply(BigDecimal.valueOf(prev))
                .add(BigDecimal.valueOf(newScore))
                .divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP);
        progress.setAvgScore(newAvg);

        if ("NOT_STARTED".equals(progress.getStatus())) {
            progress.setStatus("IN_PROGRESS");
            progress.setUnlockedAt(Instant.now());
        }

        if (progress.getLessonsCompleted() >= progress.getLessonsTotal() && progress.getLessonsTotal() > 0) {
            progress.setStatus("COMPLETED");
            progress.setCompletedAt(Instant.now());
            log.info("LearnerLevelProgressService: level COMPLETED {}/{}/{}", userId, curriculumId, cefrLevel);
        }

        return repo.save(progress);
    }
}
