package com.dsalearner.academy.controller;

import com.dsalearner.academy.dto.*;
import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.academy.service.AcademyService;
import com.dsalearner.civilization.service.LearningRewardService;
import com.dsalearner.economy.antiabuse.EconomyRateLimiter;
import com.dsalearner.economy.antiabuse.SuspiciousActivityService;
import com.dsalearner.exception.TooManyRequestsException;
import com.dsalearner.security.DomainAuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import java.util.UUID;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/v1/academy/{language}")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;
    private final LearningRewardService learningRewardService;
    private final CurrentUserProvider currentUserProvider;
    private final DomainAuthorizationService domainAuthService;
    private final EconomyRateLimiter economyRateLimiter;
    private final SuspiciousActivityService suspiciousActivityService;

    @GetMapping("/curriculum")
    public ResponseEntity<AcademyCurriculumResponse> getCurriculum(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.getCurriculum(language, userId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<AcademyLessonResponse> getLesson(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.getLesson(language, lessonId, userId));
    }

    @GetMapping("/lessons/{lessonId}/overview")
    public ResponseEntity<LessonOverviewDto> getLessonOverview(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.getLessonOverview(language, lessonId, userId));
    }

    @PatchMapping("/lessons/{lessonId}/step")
    public ResponseEntity<LessonProgressDto> updateStepProgress(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            @Valid @RequestBody UpdateStepRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.updateStepProgress(language, lessonId, userId, request.stepIndex()));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<LessonCompletionResponse> completeLesson(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            @Valid @RequestBody CompleteLessonRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        if (!economyRateLimiter.isLessonAllowed(userId)) {
            suspiciousActivityService.flag(userId, "LESSON_RATE_LIMIT",
                    "Exceeded lesson completion rate limit for lessonId=" + lessonId);
            throw new TooManyRequestsException("Lesson completion rate limit exceeded. Please slow down.");
        }
        return ResponseEntity.ok(academyService.completeLesson(language, lessonId, userId, request.score()));
    }

    @GetMapping("/progress")
    public ResponseEntity<AcademyProgressResponse> getProgress(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.getProgress(language, userId));
    }

    /**
     * Grant a small reward for first-time exercise completion. Idempotent —
     * calling again for the same exerciseId returns coinsAwarded=0 with no side effects.
     */
    @PostMapping("/lessons/{lessonId}/exercises/{exerciseId}/complete")
    public ResponseEntity<ExerciseRewardResponse> completeExercise(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            @PathVariable String exerciseId,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        long coinsAwarded = learningRewardService.grantExerciseReward(
                userId, lessonId, exerciseId, language, 5L);
        return ResponseEntity.ok(new ExerciseRewardResponse(exerciseId, coinsAwarded));
    }

    record ExerciseRewardResponse(@NotBlank String exerciseId, @NotNull long coinsAwarded) {}
}
