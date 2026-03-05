package com.resilientmesh.transaction;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    ResponseEntity<Map<String, Object>> pay(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKeyHeader) {
        String idempotencyKey = (idempotencyKeyHeader == null || idempotencyKeyHeader.isBlank())
                ? UUID.randomUUID().toString()
                : idempotencyKeyHeader;

        Map<String, Object> debit = walletGateway.debit(request.userId(), request.amount(), idempotencyKey);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "ACCEPTED",
                "idempotencyKey", idempotencyKey,
                "userId", request.userId(),
                "amount", request.amount(),
                "walletTransaction", debit,
                "processedAt", Instant.now().toString()));
    }
}
