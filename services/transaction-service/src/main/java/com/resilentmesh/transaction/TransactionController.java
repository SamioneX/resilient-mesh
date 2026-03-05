package com.resilentmesh.transaction;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
class TransactionController {

    private final WalletGateway walletGateway;

    TransactionController(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    @PostMapping
    Map<String, Object> pay(@Valid @RequestBody PaymentRequest request) {
        Map<String, Object> wallet = walletGateway.fetchWallet(request.userId());
        return Map.of(
                "status", "ACCEPTED",
                "userId", request.userId(),
                "amount", request.amount(),
                "walletSnapshot", wallet,
                "processedAt", Instant.now().toString());
    }
}
