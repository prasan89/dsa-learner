package com.dsalearner.controller;

import com.dsalearner.dto.response.HintResponse;
import com.dsalearner.security.DomainAuthorizationService;
import com.dsalearner.service.HintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import java.util.UUID;

@ConditionalOnExpression("'${application.mode}' == 'dsa' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/problems/{slug}/hints")
@RequiredArgsConstructor
public class HintController {

    private final HintService hintService;
    private final DomainAuthorizationService domainAuthService;

    @GetMapping
    public ResponseEntity<List<HintResponse>> getHints(
            @PathVariable String slug,
            Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        domainAuthService.requireDomain(authentication, "dsa");
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(hintService.getHints(slug, userId));
    }

    @PostMapping("/{level}/unlock")
    public ResponseEntity<HintResponse> unlock(
            @PathVariable String slug,
            @PathVariable int level,
            Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        domainAuthService.requireDomain(authentication, "dsa");
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(hintService.unlockHint(slug, level, userId));
    }
}
