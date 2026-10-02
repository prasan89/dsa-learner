package com.dsalearner.economy.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EconomyEventService {

    private final LangoaEconomyEventRepository repository;

    /**
     * Record an economy analytics event. Fire-and-forget: runs in its own transaction,
     * never propagates exceptions to the caller, never causes core operations to fail.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String eventName, UUID userId, String languageCode,
                       Long amount, String currencyType, String sourceRef,
                       String correlationId, Map<String, Object> extra) {
        try {
            LangoaEconomyEvent event = LangoaEconomyEvent.builder()
                    .eventName(eventName)
                    .userId(userId)
                    .languageCode(languageCode)
                    .amount(amount)
                    .currencyType(currencyType)
                    .sourceRef(sourceRef)
                    .correlationId(correlationId)
                    .extra(extra)
                    .build();
            repository.save(event);
        } catch (Exception e) {
            log.warn("Economy event record failed (non-critical): eventName={} userId={} error={}", eventName, userId, e.getMessage());
        }
    }

    public void record(String eventName, UUID userId) {
        record(eventName, userId, null, null, null, null, null, null);
    }

    public void record(String eventName, UUID userId, String languageCode, String sourceRef) {
        record(eventName, userId, languageCode, null, null, sourceRef, null, null);
    }

    public void record(String eventName, UUID userId, String languageCode,
                       Long amount, String currencyType, String sourceRef) {
        record(eventName, userId, languageCode, amount, currencyType, sourceRef, null, null);
    }
}
