package com.resilientmesh.transaction;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class WalletGateway {

    private final RestClient restClient;

    WalletGateway(@Value("${clients.wallet.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofMillis(600).toMillis());
                    setReadTimeout((int) Duration.ofMillis(900).toMillis());
                }})
                .build();
    }

    @Retry(name = "walletDebit")
    @CircuitBreaker(name = "walletDebit", fallbackMethod = "debitFallback")
    Map<String, Object> debit(String userId, BigDecimal amount, String idempotencyKey) {
        return restClient.post()
                .uri("/api/wallets/{userId}/debit", userId)
                .header("Idempotency-Key", idempotencyKey)
                .body(Map.of("amount", amount))
                .retrieve()
                .body(Map.class);
    }

    Map<String, Object> debitFallback(String userId, BigDecimal amount, String idempotencyKey, Throwable throwable) {
        return Map.of(
                "idempotencyKey", idempotencyKey,
                "userId", userId,
                "amount", amount,
                "status", "degraded",
                "balanceAfter", 0,
                "currency", "USD",
                "walletVersion", -1,
                "processedAt", Instant.now().toString());
    }
}
