package com.resilientmesh.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
class WalletController {

    private final WalletClient walletClient;

    WalletController(WalletClient walletClient) {
        this.walletClient = walletClient;
    }

    @GetMapping("/{userId}")
    Map<String, Object> wallet(@PathVariable String userId) {
        String tier = walletClient.resolveUserTier(userId);
        BigDecimal balance = "PREMIUM".equals(tier) ? new BigDecimal("1000.00") : new BigDecimal("200.00");

        return Map.of(
                "userId", userId,
                "balance", balance,
                "currency", "USD",
                "tier", tier,
                "fetchedAt", Instant.now().toString());
    }
}
