package com.dsalearner.subscription.service;

import com.dsalearner.subscription.dto.SubscriptionStatusDto;
import com.dsalearner.subscription.entity.LangSubscription;
import com.dsalearner.subscription.repository.LangSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RequiredArgsConstructor
@Slf4j
public class EntitlementService {

    private final LangSubscriptionRepository subscriptionRepository;

    public boolean isPro(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(LangSubscription::isProActive)
                .orElse(false);
    }

    public boolean hasEntitlement(UUID userId, String feature) {
        return isPro(userId);
    }

    public SubscriptionStatusDto getStatus(UUID userId) {
        LangSubscription sub = subscriptionRepository.findByUserId(userId)
                .orElse(null);
        if (sub == null) {
            return new SubscriptionStatusDto("FREE", false, null, null);
        }
        return new SubscriptionStatusDto(
                sub.getPlanCode(),
                sub.isProActive(),
                sub.getCurrentPeriodStart(),
                sub.getCurrentPeriodEnd()
        );
    }
}
