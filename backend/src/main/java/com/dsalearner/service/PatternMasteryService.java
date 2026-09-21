package com.dsalearner.service;

import com.dsalearner.dto.response.PatternMasteryResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatternMasteryService {

    private final PatternMasteryRepository masteryRepository;
    private final PatternRepository patternRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

    public List<PatternMasteryResponse> getAllForUser(UUID userId) {
        List<Pattern> allPatterns = patternRepository.findAll(
                org.springframework.data.domain.Sort.by("displayOrder"));
        List<PatternMastery> existing = masteryRepository.findByUserId(userId);

        return allPatterns.stream().map(p -> {
            PatternMastery mastery = existing.stream()
                    .filter(m -> m.getPattern().getId().equals(p.getId()))
                    .findFirst()
                    .orElse(null);
            String status = mastery != null ? mastery.getStatus().name() : "NOT_STARTED";
            double score = mastery != null ? mastery.getMasteryScore().doubleValue() : 0.0;
            int solved = mastery != null ? mastery.getProblemsSolved() : 0;
            return new PatternMasteryResponse(p.getId(), p.getSlug(), p.getName(), status, score, solved);
        }).toList();
    }

    @Transactional
    public PatternMasteryResponse updateMastery(String patternSlug, UUID userId, String status) {
        Pattern pattern = patternRepository.findBySlug(patternSlug)
                .orElseThrow(() -> new NotFoundException("Pattern not found: " + patternSlug));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PatternMastery.MasteryStatus masteryStatus = PatternMastery.MasteryStatus.valueOf(status);
        PatternMastery mastery = masteryRepository
                .findByUserIdAndPatternId(userId, pattern.getId())
                .orElse(PatternMastery.builder().user(user).pattern(pattern).build());

        mastery.setStatus(masteryStatus);
        mastery.setUpdatedAt(Instant.now());
        masteryRepository.save(mastery);

        return new PatternMasteryResponse(pattern.getId(), pattern.getSlug(), pattern.getName(),
                status, mastery.getMasteryScore().doubleValue(), mastery.getProblemsSolved());
    }

    /**
     * Recalculate mastery score for all patterns of a user after a problem is solved.
     * Score formula:
     *   base = (solved / total) * 100
     *   attempt penalty = max(0, (attempts - 1) * 3) per problem, capped at 15
     *   hint penalty = hints_used * 5 per problem, capped at 15
     *   final = base - avg(penalties), clamped to [0, 100]
     */
    @Transactional
    public void recalculateMasteryForUser(UUID userId) {
        List<Pattern> allPatterns = patternRepository.findAll();
        User user = userRepository.getReferenceById(userId);

        for (Pattern pattern : allPatterns) {
            List<UserProgress> progresses = userProgressRepository.findByUserIdAndPatternId(userId, pattern.getId());
            if (progresses.isEmpty()) continue;

            long totalProblems = userProgressRepository.countProblemsByPatternId(pattern.getId());
            if (totalProblems == 0) continue;

            long solved = progresses.stream().filter(UserProgress::isSolved).count();
            long attempted = progresses.size();

            double totalPenalty = progresses.stream()
                    .filter(UserProgress::isSolved)
                    .mapToDouble(up -> {
                        double attemptPenalty = Math.min(15, Math.max(0, (up.getAttempts() - 1) * 3.0));
                        double hintPenalty = Math.min(15, up.getHintsUsed() * 5.0);
                        return attemptPenalty + hintPenalty;
                    })
                    .sum();

            double avgPenalty = solved > 0 ? totalPenalty / solved : 0;
            double baseScore = (solved * 100.0) / totalProblems;
            double finalScore = Math.max(0, Math.min(100, baseScore - avgPenalty));

            PatternMastery.MasteryStatus autoStatus;
            if (solved == 0) autoStatus = PatternMastery.MasteryStatus.NOT_STARTED;
            else if (finalScore < 30) autoStatus = PatternMastery.MasteryStatus.LEARNING;
            else if (finalScore < 70) autoStatus = PatternMastery.MasteryStatus.PRACTICED;
            else autoStatus = PatternMastery.MasteryStatus.MASTERED;

            PatternMastery mastery = masteryRepository
                    .findByUserIdAndPatternId(userId, pattern.getId())
                    .orElse(PatternMastery.builder().user(user).pattern(pattern).build());

            mastery.setMasteryScore(BigDecimal.valueOf(finalScore).setScale(2, RoundingMode.HALF_UP));
            mastery.setProblemsSolved((int) solved);
            mastery.setProblemsAttempted((int) attempted);
            mastery.setStatus(autoStatus);
            mastery.setUpdatedAt(Instant.now());
            masteryRepository.save(mastery);
        }
    }
}

