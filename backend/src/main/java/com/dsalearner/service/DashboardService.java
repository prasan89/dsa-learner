package com.dsalearner.service;

import com.dsalearner.dto.response.DashboardResponse;
import com.dsalearner.model.entity.UserActivity;
import com.dsalearner.model.entity.UserSubscription;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserProgressRepository     userProgressRepository;
    private final PatternRepository          patternRepository;
    private final PatternMasteryRepository   patternMasteryRepository;
    private final UserActivityRepository     userActivityRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ProblemRepository          problemRepository;

    public DashboardResponse getDashboard(UUID userId) {
        int streak = calculateStreak(userId);

        // DSA stats
        long dsaTotal  = patternRepository.findByCategoryOrderByDisplayOrder("DSA")
                .stream()
                .mapToLong(p -> userProgressRepository.countProblemsByPatternId(p.getId()))
                .sum();
        long dsaSolved = userProgressRepository.countByUserIdAndSolvedTrue(userId);

        double dsaMastery = patternMasteryRepository.findByUserId(userId).stream()
                .filter(pm -> !"SYSTEM_DESIGN".equals(pm.getPattern().getCategory()))
                .mapToDouble(pm -> pm.getMasteryScore().doubleValue())
                .average()
                .orElse(0.0);

        // System design stats
        long sdTotal = patternRepository.findByCategoryOrderByDisplayOrder("SYSTEM_DESIGN").size();
        long sdMastered = patternMasteryRepository.findByUserId(userId).stream()
                .filter(pm -> "SYSTEM_DESIGN".equals(pm.getPattern().getCategory()))
                .filter(pm -> pm.getStatus().name().equals("MASTERED"))
                .count();

        double sdMastery = patternMasteryRepository.findByUserId(userId).stream()
                .filter(pm -> "SYSTEM_DESIGN".equals(pm.getPattern().getCategory()))
                .mapToDouble(pm -> pm.getMasteryScore().doubleValue())
                .average()
                .orElse(0.0);

        // Subscription plan
        String plan = userSubscriptionRepository.findByUserId(userId)
                .map(s -> s.isPro() ? "PRO" : "FREE")
                .orElse("FREE");

        // Next recommended action — first unsolved problem in the user's lowest-mastery DSA pattern
        DashboardResponse.NextAction nextAction = findNextAction(userId);

        return new DashboardResponse(
                streak,
                new DashboardResponse.DsaStats(dsaTotal, dsaSolved, Math.round(dsaMastery * 10.0) / 10.0),
                new DashboardResponse.SystemDesignStats(sdTotal, sdMastered, Math.round(sdMastery * 10.0) / 10.0),
                plan,
                nextAction
        );
    }

    // Record today's activity (called after any submission or review completion)
    public void recordActivity(UUID userId) {
        LocalDate today = LocalDate.now();
        if (!userActivityRepository.existsByUserIdAndActivityDate(userId, today)) {
            userActivityRepository.save(UserActivity.builder()
                    .userId(userId)
                    .activityDate(today)
                    .build());
        }
    }

    private int calculateStreak(UUID userId) {
        List<LocalDate> dates = userActivityRepository.findDatesByUserIdOrderByDateDesc(userId);
        if (dates.isEmpty()) return 0;

        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Streak is alive only if the user was active today or yesterday
        if (!dates.get(0).equals(today) && !dates.get(0).equals(yesterday)) return 0;

        int streak = 0;
        LocalDate expected = dates.get(0);
        for (LocalDate date : dates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private DashboardResponse.NextAction findNextAction(UUID userId) {
        // Find the DSA pattern with lowest mastery score that has unsolved problems
        return patternRepository.findByCategoryOrderByDisplayOrder("DSA").stream()
                .flatMap(pattern -> {
                    // Get first unsolved problem in this pattern
                    var page = problemRepository.findAllByActiveTrueAndPatternId(
                            pattern.getId(),
                            org.springframework.data.domain.PageRequest.of(0, 50));
                    var solvedIds = userProgressRepository.findSolvedProblemIdsByUserId(userId);
                    return page.getContent().stream()
                            .filter(p -> !solvedIds.contains(p.getId()))
                            .findFirst()
                            .map(p -> new DashboardResponse.NextAction(
                                    p.getSlug(), p.getTitle(), pattern.getName(),
                                    p.getDifficulty().name()))
                            .stream();
                })
                .findFirst()
                .orElse(null);
    }
}
