package com.dsalearner.academy.service;

import com.dsalearner.academy.model.entity.LearnerLessonProgress;
import com.dsalearner.academy.repository.LearnerLessonProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerLessonProgressService {

    private final LearnerLessonProgressRepository repo;

    /**
     * Returns the progress record for a user+lesson pair, creating a NOT_STARTED
     * record if one does not exist yet.
     */
    @Transactional
    public LearnerLessonProgress getOrCreate(UUID userId, UUID lessonId) {
        return repo.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    LearnerLessonProgress p = LearnerLessonProgress.builder()
                            .userId(userId)
                            .lessonId(lessonId)
                            .status("NOT_STARTED")
                            .stepIndex(0)
                            .build();
                    LearnerLessonProgress saved = repo.save(p);
                    log.debug("LearnerLessonProgressService: created NOT_STARTED record for user={} lesson={}", userId, lessonId);
                    return saved;
                });
    }

    @Transactional(readOnly = true)
    public Optional<LearnerLessonProgress> find(UUID userId, UUID lessonId) {
        return repo.findByUserIdAndLessonId(userId, lessonId);
    }

    @Transactional(readOnly = true)
    public List<LearnerLessonProgress> findAllForUser(UUID userId) {
        return repo.findByUserId(userId);
    }

    /**
     * Transitions NOT_STARTED → IN_PROGRESS on first interaction.
     * Idempotent: if already IN_PROGRESS or COMPLETED, step index is still updated.
     */
    @Transactional
    public LearnerLessonProgress startOrAdvance(UUID userId, UUID lessonId, int stepIndex) {
        LearnerLessonProgress progress = getOrCreate(userId, lessonId);
        Instant now = Instant.now();

        if ("NOT_STARTED".equals(progress.getStatus())) {
            progress.setStatus("IN_PROGRESS");
            progress.setStartedAt(now);
        }
        progress.setStepIndex(stepIndex);
        progress.setLastInteractionAt(now);
        LearnerLessonProgress saved = repo.save(progress);
        log.debug("LearnerLessonProgressService: user={} lesson={} advanced to step={}", userId, lessonId, stepIndex);
        return saved;
    }

    /**
     * Marks a lesson as COMPLETED, recording the final score (0–100).
     * Idempotent: already-completed lessons are not re-completed.
     */
    @Transactional
    public LearnerLessonProgress complete(UUID userId, UUID lessonId, int score) {
        if (score < 0 || score > 100) throw new IllegalArgumentException("score must be 0–100, got " + score);
        LearnerLessonProgress progress = getOrCreate(userId, lessonId);
        if ("COMPLETED".equals(progress.getStatus())) return progress;

        Instant now = Instant.now();
        progress.setStatus("COMPLETED");
        progress.setScore((short) score);
        progress.setCompletedAt(now);
        progress.setLastInteractionAt(now);
        LearnerLessonProgress saved = repo.save(progress);
        log.info("LearnerLessonProgressService: user={} lesson={} COMPLETED score={}", userId, lessonId, score);
        return saved;
    }

    @Transactional(readOnly = true)
    public long countCompleted(UUID userId) {
        return repo.countCompletedByUser(userId);
    }
}
