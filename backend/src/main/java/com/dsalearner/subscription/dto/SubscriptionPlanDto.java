package com.dsalearner.subscription.dto;

public record SubscriptionPlanDto(
        String planCode,
        String displayName,
        int pricePaise,
        String currency,
        int intervalDays,
        String playProductId
) {}
