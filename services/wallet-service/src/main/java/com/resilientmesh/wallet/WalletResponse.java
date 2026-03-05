package com.resilientmesh.wallet;

import java.math.BigDecimal;
import java.time.Instant;

record WalletResponse(String userId, BigDecimal balance, String currency, String tier, Long version, Instant fetchedAt) {
}
