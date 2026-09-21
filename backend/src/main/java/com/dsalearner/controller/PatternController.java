package com.dsalearner.controller;

import com.dsalearner.dto.response.PatternMasteryResponse;
import com.dsalearner.dto.response.PatternResponse;
import com.dsalearner.service.PatternMasteryService;
import com.dsalearner.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternService patternService;
    private final PatternMasteryService masteryService;

    @GetMapping
    public ResponseEntity<List<PatternResponse>> list(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            UUID userId = UUID.fromString(userDetails.getUsername());
            return ResponseEntity.ok(patternService.findAllForUser(userId));
        }
        return ResponseEntity.ok(patternService.findAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PatternResponse> get(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            UUID userId = UUID.fromString(userDetails.getUsername());
            return ResponseEntity.ok(patternService.findBySlugForUser(slug, userId));
        }
        return ResponseEntity.ok(patternService.findBySlug(slug));
    }

    @GetMapping("/mastery")
    public ResponseEntity<List<PatternMasteryResponse>> getMastery(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(masteryService.getAllForUser(userId));
    }

    @PutMapping("/{slug}/mastery")
    public ResponseEntity<PatternMasteryResponse> updateMastery(
            @PathVariable String slug,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String status = body.get("status");
        return ResponseEntity.ok(masteryService.updateMastery(slug, userId, status));
    }
}
