package com.dsalearner.controller;

import com.dsalearner.dto.response.SubmissionResponse;
import com.dsalearner.dto.response.UserProgressResponse;
import com.dsalearner.service.SpacedRepetitionService;
import com.dsalearner.service.SubmissionService;
import com.dsalearner.service.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserProgressService userProgressService;
    private final SubmissionService submissionService;
    private final SpacedRepetitionService spacedRepetitionService;

    @GetMapping("/progress")
    public ResponseEntity<UserProgressResponse> progress(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(userProgressService.getProgress(userId));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<SubmissionResponse>> recentSubmissions(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(submissionService.listForUser(userId));
    }

    @GetMapping("/reviews/today")
    public ResponseEntity<List<SpacedRepetitionService.ReviewItem>> todayReviews(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(spacedRepetitionService.getTodayReviews(userId));
    }

    @PostMapping("/reviews/{problemId}/complete")
    public ResponseEntity<Void> completeReview(
            @PathVariable UUID problemId,
            @RequestParam(defaultValue = "4") int quality,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        spacedRepetitionService.completeReview(userId, problemId, quality);
        return ResponseEntity.ok().build();
    }
}

