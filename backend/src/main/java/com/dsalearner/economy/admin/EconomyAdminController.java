package com.dsalearner.economy.admin;

import com.dsalearner.economy.analytics.LangoaEconomyEventRepository;
import com.dsalearner.economy.antiabuse.LangoaSuspiciousActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/internal/economy")
@RequiredArgsConstructor
public class EconomyAdminController {

    private final LangoaEconomyEventRepository economyEventRepo;
    private final LangoaSuspiciousActivityRepository suspiciousRepo;

    @Value("${admin.key:}")
    private String adminKey;

    /** Summary: event counts grouped by event name (last 24h by default). */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestHeader(value = "X-Admin-Key", required = false) String key) {
        if (!isAuthorized(key)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        List<Object[]> counts = economyEventRepo.countByEventName();
        long eventCount24h = economyEventRepo.count();
        return ResponseEntity.ok(Map.of(
                "eventsByName", counts.stream().map(row -> Map.of(
                        "eventName", row[0],
                        "count", row[1])).toList(),
                "totalEconomyEvents", eventCount24h
        ));
    }

    /** List recent suspicious activity flags (last 50). */
    @GetMapping("/suspicious")
    public ResponseEntity<?> getSuspiciousActivity(
            @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        if (!isAuthorized(key)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        var flags = suspiciousRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50));
        return ResponseEntity.ok(flags.stream().map(f -> Map.of(
                "id", f.getId(),
                "userId", f.getUserId() != null ? f.getUserId() : "null",
                "reason", f.getReason(),
                "detail", f.getDetail() != null ? f.getDetail() : "",
                "createdAt", f.getCreatedAt()
        )).toList());
    }

    /** Suspicious activity for a specific user. */
    @GetMapping("/suspicious/{userId}")
    public ResponseEntity<?> getSuspiciousForUser(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        if (!isAuthorized(key)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        var flags = suspiciousRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 20));
        return ResponseEntity.ok(flags);
    }

    /** Count suspicious flags for a user in the past hour (quick abuse check). */
    @GetMapping("/suspicious/{userId}/count")
    public ResponseEntity<?> countRecentSuspicious(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-Admin-Key", required = false) String key) {
        if (!isAuthorized(key)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        long count = suspiciousRepo.countByUserIdAndCreatedAtAfter(userId, Instant.now().minus(1, ChronoUnit.HOURS));
        return ResponseEntity.ok(Map.of("userId", userId, "flagsLastHour", count));
    }

    private boolean isAuthorized(String key) {
        return adminKey != null && !adminKey.isBlank() && adminKey.equals(key);
    }
}
