package com.resilentmesh.wallet;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class WalletClient {

    private final RestClient restClient;

    WalletClient(@Value("${clients.user.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) Duration.ofMillis(600).toMillis());
                    setReadTimeout((int) Duration.ofMillis(900).toMillis());
                }})
                .build();
    }

    @Retry(name = "userLookup")
    @CircuitBreaker(name = "userLookup", fallbackMethod = "defaultTier")
    String resolveUserTier(String userId) {
        Map<?, ?> response = restClient.get()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .body(Map.class);

        Object tier = Objects.requireNonNull(response).get("tier");
        return tier == null ? "STANDARD" : tier.toString();
    }

    String defaultTier(String userId, Throwable throwable) {
        return "STANDARD";
    }
}
