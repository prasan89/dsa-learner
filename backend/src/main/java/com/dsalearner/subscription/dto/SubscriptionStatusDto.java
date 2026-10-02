package com.dsalearner.subscription.dto;

import java.time.Instant;

public record SubscriptionStatusDto(
        String planCode,
        boolean isPro,
        Instant currentPeriodStart,
        Instant currentPeriodEnd
) {}
