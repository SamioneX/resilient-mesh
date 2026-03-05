package com.resilientmesh.transaction;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
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

    @Retry(name = "walletLookup")
    @CircuitBreaker(name = "walletLookup", fallbackMethod = "fallbackWallet")
    Map<String, Object> fetchWallet(String userId) {
        return restClient.get()
                .uri("/api/wallets/{userId}", userId)
                .retrieve()
                .body(Map.class);
    }

    Map<String, Object> fallbackWallet(String userId, Throwable throwable) {
        return Map.of(
                "userId", userId,
                "balance", 0,
                "currency", "USD",
                "tier", "DEGRADED");
    }
}
