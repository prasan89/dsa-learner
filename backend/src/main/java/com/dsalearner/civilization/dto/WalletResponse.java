package com.dsalearner.civilization.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record WalletResponse(
        Map<String, Long> balances,
        List<TransactionSummary> recentTransactions
) {
    public record TransactionSummary(
            String type,
            String currency,
            long amount,
            long balanceAfter,
            Instant createdAt
    ) {}
}
