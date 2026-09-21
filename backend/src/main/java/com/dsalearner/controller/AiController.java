package com.dsalearner.controller;

import com.dsalearner.dto.request.AiReviewRequest;
import com.dsalearner.dto.request.PatternDetectRequest;
import com.dsalearner.dto.response.AiReviewResponse;
import com.dsalearner.dto.response.PatternDetectResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.Problem;
import com.dsalearner.repository.ProblemRepository;
import com.dsalearner.service.AiService;
import com.dsalearner.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final RateLimitService rateLimitService;
    private final ProblemRepository problemRepository;

    @PostMapping("/review")
    public ResponseEntity<AiReviewResponse> review(
            @RequestBody AiReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());

        if (!rateLimitService.allowAiReview(userId)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Daily AI review limit (5) reached. Try again tomorrow.");
        }

        Problem problem = problemRepository.findBySlug(request.problemSlug())
                .orElseThrow(() -> new NotFoundException("Problem not found: " + request.problemSlug()));

        AiReviewResponse response = aiService.reviewCode(
                problem.getTitle(), problem.getDescription(), request.code());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detect-pattern")
    public ResponseEntity<PatternDetectResponse> detectPattern(
            @RequestBody PatternDetectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(aiService.detectPattern(request.code()));
    }

    @GetMapping("/review/remaining")
    public ResponseEntity<Map<String, Integer>> remaining(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("remaining", rateLimitService.remainingAiReviews(userId)));
    }
}
