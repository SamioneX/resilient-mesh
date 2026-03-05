package com.resilientmesh.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallets")
class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 64)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 8)
    private String currency;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Wallet() {
    }

    static Wallet create(String userId, BigDecimal initialBalance, String currency, Instant now) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet.balance = initialBalance;
        wallet.currency = currency;
        wallet.createdAt = now;
        wallet.updatedAt = now;
        return wallet;
    }

    void debit(BigDecimal amount, Instant now) {
        this.balance = this.balance.subtract(amount);
        this.updatedAt = now;
    }

    String getUserId() {
        return userId;
    }

    BigDecimal getBalance() {
        return balance;
    }

    String getCurrency() {
        return currency;
    }

    Long getVersion() {
        return version;
    }
}
