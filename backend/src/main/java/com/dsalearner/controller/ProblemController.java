package com.dsalearner.controller;

import com.dsalearner.dto.response.PageResponse;
import com.dsalearner.dto.response.ProblemResponse;
import com.dsalearner.dto.response.ProblemSummaryResponse;
import com.dsalearner.dto.response.QualityCheckResponse;
import com.dsalearner.security.DomainAuthorizationService;
import com.dsalearner.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import java.util.UUID;

@ConditionalOnExpression("'${application.mode}' == 'dsa' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;
    private final DomainAuthorizationService domainAuthService;

    @GetMapping
    public ResponseEntity<PageResponse<ProblemSummaryResponse>> list(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String patternId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        domainAuthService.requireDomain(authentication, "dsa");
        UUID userId = userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
        return ResponseEntity.ok(problemService.findAll(difficulty, patternId, page, size, userId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProblemResponse> get(
            @PathVariable String slug,
            Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        domainAuthService.requireDomain(authentication, "dsa");
        UUID userId = userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
        return ResponseEntity.ok(problemService.findBySlug(slug, userId));
    }

    @GetMapping("/{slug}/quality-check")
    public ResponseEntity<QualityCheckResponse> qualityCheck(
            @PathVariable String slug,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "dsa");
        return ResponseEntity.ok(problemService.getQualityCheck(slug));
    }
}
