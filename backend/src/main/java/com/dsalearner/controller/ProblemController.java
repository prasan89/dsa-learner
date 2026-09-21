package com.dsalearner.controller;

import com.dsalearner.dto.response.PageResponse;
import com.dsalearner.dto.response.ProblemResponse;
import com.dsalearner.dto.response.ProblemSummaryResponse;
import com.dsalearner.security.JwtService;
import com.dsalearner.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<PageResponse<ProblemSummaryResponse>> list(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String patternId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
        return ResponseEntity.ok(problemService.findAll(difficulty, patternId, page, size, userId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProblemResponse> get(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = userDetails != null ? UUID.fromString(userDetails.getUsername()) : null;
        return ResponseEntity.ok(problemService.findBySlug(slug, userId));
    }
}
