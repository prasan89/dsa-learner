package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    // SM-2 intervals: 1, 3, 7, 14, 30 days
    private static final int[] BASE_INTERVALS = {1, 3, 7, 14, 30};

    private final SpacedRepetitionRepository srRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public record ReviewItem(UUID problemId, String slug, String title, String difficulty,
                              LocalDate dueDate, int repetition) {}

    public List<ReviewItem> getTodayReviews(UUID userId) {
        return srRepository.findDueReviews(userId, LocalDate.now()).stream()
                .map(r -> new ReviewItem(
                        r.getProblem().getId(),
                        r.getProblem().getSlug(),
                        r.getProblem().getTitle(),
                        r.getProblem().getDifficulty().name(),
                        r.getDueDate(),
                        r.getRepetition()))
                .toList();
    }

    public long countTodayDue(UUID userId) {
        return srRepository.countByUserIdAndDueDateAndCompletedAtIsNull(userId, LocalDate.now());
    }

    @Transactional
    public void scheduleAfterSolve(UUID userId, UUID problemId) {
        User user = userRepository.getReferenceById(userId);
        Problem problem = problemRepository.getReferenceById(problemId);

        // Only schedule if no pending review exists
        srRepository.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId)
                .ifPresentOrElse(
                        existing -> {
                            // already has a review entry — don't add duplicate
                        },
                        () -> {
                            SpacedRepetitionReview review = SpacedRepetitionReview.builder()
                                    .user(user)
                                    .problem(problem)
                                    .dueDate(LocalDate.now().plusDays(BASE_INTERVALS[0]))
                                    .intervalDays(BASE_INTERVALS[0])
                                    .repetition(0)
                                    .easeFactor(new BigDecimal("2.50"))
                                    .build();
                            srRepository.save(review);
                        });
    }

    @Transactional
    public void completeReview(UUID userId, UUID problemId, int quality) {
        // quality: 0-5 (SM-2 scale). 4 = good, 5 = perfect, <3 = failed
        srRepository.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId)
                .ifPresent(review -> {
                    review.setCompletedAt(java.time.Instant.now());

                    // SM-2 algorithm
                    int rep = review.getRepetition();
                    BigDecimal ef = review.getEaseFactor();
                    int nextInterval;

                    if (quality >= 3) {
                        nextInterval = rep < BASE_INTERVALS.length
                                ? BASE_INTERVALS[Math.min(rep + 1, BASE_INTERVALS.length - 1)]
                                : (int) (review.getIntervalDays() * ef.doubleValue());
                        BigDecimal newEf = ef.add(BigDecimal.valueOf(0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));
                        review.setEaseFactor(newEf.max(new BigDecimal("1.30")));
                        review.setRepetition(rep + 1);
                    } else {
                        nextInterval = 1;
                        review.setRepetition(0);
                    }

                    srRepository.save(review);

                    // Schedule next review
                    SpacedRepetitionReview next = SpacedRepetitionReview.builder()
                            .user(review.getUser())
                            .problem(review.getProblem())
                            .dueDate(LocalDate.now().plusDays(nextInterval))
                            .intervalDays(nextInterval)
                            .repetition(review.getRepetition())
                            .easeFactor(review.getEaseFactor())
                            .build();
                    srRepository.save(next);
                });
    }
}
