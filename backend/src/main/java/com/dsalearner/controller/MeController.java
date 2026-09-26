package com.dsalearner.controller;

import com.dsalearner.security.DomainAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Lightweight identity endpoint used by the frontend to determine the
 * authenticated user's active learning domain without requiring a full
 * profile fetch.
 */
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final DomainAuthorizationService domainAuthService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String activeDomain = domainAuthService.getActiveDomain(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId.toString(),
                "activeDomain", activeDomain != null ? activeDomain : "none"
        ));
    }
}
