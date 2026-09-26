package com.dsalearner.academy.controller;

import com.dsalearner.academy.dto.*;
import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.academy.service.AcademyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academy/{language}")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/curriculum")
    public ResponseEntity<AcademyCurriculumResponse> getCurriculum(
            @PathVariable String language,
            Authentication authentication) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.getCurriculum(language, userId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<AcademyLessonResponse> getLesson(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            Authentication authentication) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.getLesson(language, lessonId, userId));
    }

    @PatchMapping("/lessons/{lessonId}/step")
    public ResponseEntity<LessonProgressDto> updateStepProgress(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            @Valid @RequestBody UpdateStepRequest request,
            Authentication authentication) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.updateStepProgress(language, lessonId, userId, request.stepIndex()));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<LessonCompletionResponse> completeLesson(
            @PathVariable String language,
            @PathVariable UUID lessonId,
            @Valid @RequestBody CompleteLessonRequest request,
            Authentication authentication) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.completeLesson(language, lessonId, userId, request.score()));
    }

    @GetMapping("/progress")
    public ResponseEntity<AcademyProgressResponse> getProgress(
            @PathVariable String language,
            Authentication authentication) {
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(academyService.getProgress(language, userId));
    }
}
