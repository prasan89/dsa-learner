package com.dsalearner.controller;

import com.dsalearner.dto.request.AiReviewRequest;
import com.dsalearner.dto.request.PatternDetectRequest;
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

    @PostMapping("/review")
    public ResponseEntity<AiReviewResponse> review(
            @RequestBody AiReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());

        if (!creditService.deductForReview(userId)) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Insufficient AI credits. Please purchase more credits.");
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
