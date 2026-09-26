package com.dsalearner.controller;

import com.dsalearner.dto.request.CodeExecutionRequest;
import com.dsalearner.dto.response.RunResultResponse;
import com.dsalearner.dto.response.SubmissionResponse;
import com.dsalearner.security.DomainAuthorizationService;
import com.dsalearner.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/problems/{slug}")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final DomainAuthorizationService domainAuthService;

    @PostMapping("/run")
    public ResponseEntity<RunResultResponse> run(
            @PathVariable String slug,
            @Valid @RequestBody CodeExecutionRequest req,
            Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        domainAuthService.requireDomain(authentication, "dsa");
        UUID userId = userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
        return ResponseEntity.ok(submissionService.run(slug, req, userId));
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmissionResponse> submit(
            @PathVariable String slug,
            @Valid @RequestBody CodeExecutionRequest req,
            Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        domainAuthService.requireDomain(authentication, "dsa");
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(submissionService.submit(slug, req, userId));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<SubmissionResponse>> submissions(
            @PathVariable String slug,
            Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        domainAuthService.requireDomain(authentication, "dsa");
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(submissionService.listForProblem(slug, userId));
    }
}
