package com.dsalearner.civilization.service;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.TransactionType;
import com.dsalearner.civilization.model.entity.LangoaExerciseCompletion;
import com.dsalearner.civilization.repository.LangoaExerciseCompletionRepository;
import com.dsalearner.civilization.repository.LangoaMilestoneRewardRepository;
import com.dsalearner.civilization.repository.LangoaTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Centralizes all learning-driven reward grants so coin amounts are never scattered
 * through controllers or use-cases. Every reward path goes through here.
 *
 * Reward amounts come from {@code langoa_milestone_rewards} (configurable in DB).
 * All operations are idempotent: duplicate requests return zero without error.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningRewardService {

    private final CivilizationService civilizationService;
    private final LangoaMilestoneRewardRepository milestoneRepo;
    private final LangoaExerciseCompletionRepository exerciseCompletionRepo;
    private final LangoaTransactionRepository transactionRepo;

    // Each public method uses REQUIRES_NEW so a duplicate-key exception inside it
    // rolls back only its own transaction, not the outer lesson-completion transaction.

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long grantPerfectLessonBonus(UUID userId, String languageCode, UUID lessonId) {
        String idemKey = "perfect-" + lessonId + "-" + userId;
        return grantMilestoneBonus(userId, languageCode, "LESSON_PERFECT",
                TransactionType.LESSON_PERFECT, lessonId.toString(), idemKey);
    }

    /**
     * Grant a unit-completion bonus when all lessons in a unit are done.
     * Idempotency key: "unit-{unitId}-{userId}"
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long grantUnitCompletionBonus(UUID userId, String languageCode, UUID unitId) {
        String idemKey = "unit-" + unitId + "-" + userId;
        return grantMilestoneBonus(userId, languageCode, "UNIT_COMPLETED",
                TransactionType.UNIT_COMPLETED, unitId.toString(), idemKey);
    }

    /**
     * Grant a level-completion bonus when a full CEFR level is unlocked.
     * Idempotency key: "level-{cefrLevel}-{curriculumId}-{userId}"
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long grantLevelCompletionBonus(UUID userId, String languageCode,
                                          String cefrLevel, UUID curriculumId) {
        String idemKey = "level-" + cefrLevel + "-" + curriculumId + "-" + userId;
        return grantMilestoneBonus(userId, languageCode, "LEVEL_COMPLETED",
                TransactionType.LEVEL_COMPLETED, cefrLevel + "@" + curriculumId, idemKey);
    }

    // ── Exercise rewards ──────────────────────────────────────────────────────

    /**
     * Grant a small reward for first-time exercise completion. Subsequent calls
     * for the same exerciseId + userId + languageCode return 0 — no farming.
     *
     * @param coinsPerExercise reward amount from the lesson's reward definition
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long grantExerciseReward(UUID userId, UUID lessonId, String exerciseId,
                                    String languageCode, long coinsPerExercise) {
        if (exerciseCompletionRepo.existsByUserIdAndExerciseIdAndLanguageCode(
                userId, exerciseId, languageCode)) {
            log.debug("Exercise already rewarded: exerciseId={} userId={}", exerciseId, userId);
            return 0L;
        }
        try {
            exerciseCompletionRepo.save(LangoaExerciseCompletion.builder()
                    .userId(userId)
                    .lessonId(lessonId)
                    .exerciseId(exerciseId)
                    .languageCode(languageCode)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // Concurrent duplicate hit the UNIQUE constraint — silent no-op
            log.debug("Exercise concurrent duplicate blocked: exerciseId={}", exerciseId);
            return 0L;
        }

        String idemKey = "exercise-" + exerciseId + "-" + userId;
        String resolvedCode = civilizationService.resolveLanguageCode(languageCode);
        civilizationService.updateBalancePublic(userId, resolvedCode, CurrencyType.COINS,
                coinsPerExercise, TransactionType.EXERCISE_REWARD, exerciseId, idemKey);
        log.debug("Exercise reward granted: exerciseId={} coins={}", exerciseId, coinsPerExercise);
        return coinsPerExercise;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private long grantMilestoneBonus(UUID userId, String languageCode, String milestoneType,
                                     TransactionType txType, String sourceRef, String idemKey) {
        long coinBonus = milestoneRepo.findByMilestoneType(milestoneType)
                .map(m -> m.getCoinBonus())
                .orElse(0L);
        if (coinBonus <= 0) return 0L;

        // Fast-path: check before attempting insert to avoid poisoning the transaction
        if (transactionRepo.existsByIdempotencyKey(idemKey)) {
            log.debug("Milestone already granted (fast path): type={} userId={}", milestoneType, userId);
            return 0L;
        }

        try {
            String resolvedCode = civilizationService.resolveLanguageCode(languageCode);
            civilizationService.updateBalancePublic(userId, resolvedCode, CurrencyType.COINS,
                    coinBonus, txType, sourceRef, idemKey);
            log.info("Milestone bonus granted: type={} coins={} userId={}", milestoneType, coinBonus, userId);
            return coinBonus;
        } catch (DataIntegrityViolationException e) {
            log.debug("Milestone duplicate blocked by constraint: type={} userId={}", milestoneType, userId);
            return 0L;
        }
    }
}
