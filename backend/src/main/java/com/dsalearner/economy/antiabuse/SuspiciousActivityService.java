package com.dsalearner.economy.antiabuse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Records suspicious economy patterns for operator review. Never auto-bans — only flags.
 * Fire-and-forget: @Async + REQUIRES_NEW so it never fails the core operation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SuspiciousActivityService {

    private final LangoaSuspiciousActivityRepository repository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void flag(UUID userId, String reason, String detail) {
        try {
            LangoaSuspiciousActivity event = LangoaSuspiciousActivity.builder()
                    .userId(userId)
                    .reason(reason)
                    .detail(detail)
                    .build();
            repository.save(event);
            log.warn("Suspicious activity flagged: userId={} reason={} detail={}", userId, reason, detail);
        } catch (Exception e) {
            log.warn("SuspiciousActivityService.flag failed (non-critical): {}", e.getMessage());
        }
    }
}
