package com.dsalearner.civilization.dto;

public record CoinPackageDto(
        String packageCode,
        String displayName,
        long coinAmount,
        int pricePaise,
        String currency,
        String playProductId
) {}
