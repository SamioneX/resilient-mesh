package com.resilientmesh.wallet;

import java.math.BigDecimal;
import java.time.Instant;

record DebitResult(
        String idempotencyKey,
        String userId,
        BigDecimal amount,
        String status,
        BigDecimal balanceAfter,
        String currency,
        Long walletVersion,
        Instant processedAt) {
}
