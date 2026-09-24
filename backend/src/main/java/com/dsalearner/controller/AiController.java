package com.dsalearner.controller;

import com.dsalearner.dto.request.AiMentorRequest;
import com.dsalearner.dto.request.AiReviewRequest;
import com.dsalearner.dto.request.PatternDetectRequest;
import com.dsalearner.dto.response.AiMentorResponse;
import com.dsalearner.dto.response.AiReviewResponse;
import com.dsalearner.dto.response.PatternDetectResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.Problem;
import com.dsalearner.repository.ProblemRepository;
import com.dsalearner.service.AiService;
import com.dsalearner.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final CreditService creditService;
    private final ProblemRepository problemRepository;

    @PostMapping("/mentor")
    public ResponseEntity<AiMentorResponse> mentor(
            @RequestBody AiMentorRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());

        if (creditService.getWallet(userId).totalCredits() < 1) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Insufficient AI credits. Please purchase more credits.");
        }

        AiMentorResponse response;
        try {
            response = aiService.mentorChat(request);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI mentor service is temporarily unavailable. Please try again shortly.");
        }

        creditService.deductForMentor(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/review")
    public ResponseEntity<AiReviewResponse> review(
            @RequestBody AiReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());

        // Check credit balance without deducting yet
        if (creditService.getWallet(userId).totalCredits() < 1) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Insufficient AI credits. Please purchase more credits.");
        }

        Problem problem = problemRepository.findBySlug(request.problemSlug())
                .orElseThrow(() -> new NotFoundException("Problem not found: " + request.problemSlug()));

        // Call AI — only deduct credit after a successful response
        AiReviewResponse response;
        try {
            response = aiService.reviewCode(
                    problem.getTitle(), problem.getDescription(), request.code());
        } catch (Exception e) {
            // AI call failed — no credit consumed
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI review service is temporarily unavailable. Please try again shortly.");
        }

        // Deduct now that we have a valid response
        creditService.deductForReview(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detect-pattern")
    public ResponseEntity<PatternDetectResponse> detectPattern(
            @RequestBody PatternDetectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        creditService.deductForDetect(userId);
        return ResponseEntity.ok(aiService.detectPattern(request.code()));
    }

    @GetMapping("/wallet")
    public ResponseEntity<CreditService.WalletResponse> wallet(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(creditService.getWallet(userId));
    }
}
