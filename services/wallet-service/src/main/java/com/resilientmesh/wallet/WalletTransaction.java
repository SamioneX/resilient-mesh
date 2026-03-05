package com.resilientmesh.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_transactions")
class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WalletTransactionStatus status;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WalletTransaction() {
    }

    static WalletTransaction create(
            String idempotencyKey,
            String userId,
            BigDecimal amount,
            WalletTransactionStatus status,
            BigDecimal balanceAfter,
            Instant createdAt) {
        WalletTransaction tx = new WalletTransaction();
        tx.idempotencyKey = idempotencyKey;
        tx.userId = userId;
        tx.amount = amount;
        tx.status = status;
        tx.balanceAfter = balanceAfter;
        tx.createdAt = createdAt;
        return tx;
    }

    String getIdempotencyKey() {
        return idempotencyKey;
    }

    String getUserId() {
        return userId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    WalletTransactionStatus getStatus() {
        return status;
    }

    BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
