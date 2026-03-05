package com.resilientmesh.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class WalletDomainService {

    private static final String USD = "USD";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletClient walletClient;

    WalletDomainService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            WalletClient walletClient) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletClient = walletClient;
    }

    @Transactional
    WalletResponse fetchWallet(String userId) {
        String tier = walletClient.resolveUserTier(userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(Wallet.create(userId, initialBalanceForTier(tier), USD, Instant.now())));

        return new WalletResponse(
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                tier,
                wallet.getVersion(),
                Instant.now());
    }

    @Transactional
    DebitResult debit(String userId, BigDecimal amount, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }

        WalletTransaction existing = walletTransactionRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
            return toResult(existing, wallet.getCurrency(), wallet.getVersion());
        }

        String tier = walletClient.resolveUserTier(userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(Wallet.create(userId, initialBalanceForTier(tier), USD, Instant.now())));

        Instant now = Instant.now();
        WalletTransactionStatus status;
        if (wallet.getBalance().compareTo(amount) < 0) {
            status = WalletTransactionStatus.REJECTED_INSUFFICIENT_FUNDS;
        } else {
            wallet.debit(amount, now);
            status = WalletTransactionStatus.APPLIED;
        }

        try {
            Wallet savedWallet = walletRepository.saveAndFlush(wallet);
            WalletTransaction savedTx = walletTransactionRepository.saveAndFlush(WalletTransaction.create(
                    idempotencyKey,
                    userId,
                    amount,
                    status,
                    savedWallet.getBalance(),
                    now));

            return toResult(savedTx, savedWallet.getCurrency(), savedWallet.getVersion());
        } catch (DataIntegrityViolationException duplicateIdempotency) {
            WalletTransaction duplicated = walletTransactionRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> duplicateIdempotency);
            Wallet currentWallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> duplicateIdempotency);
            return toResult(duplicated, currentWallet.getCurrency(), currentWallet.getVersion());
        } catch (ObjectOptimisticLockingFailureException staleWrite) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Concurrent wallet update detected. Retry request.");
        }
    }

    private DebitResult toResult(WalletTransaction tx, String currency, Long walletVersion) {
        return new DebitResult(
                tx.getIdempotencyKey(),
                tx.getUserId(),
                tx.getAmount(),
                tx.getStatus().name().toLowerCase(Locale.ROOT),
                tx.getBalanceAfter(),
                currency,
                walletVersion,
                tx.getCreatedAt());
    }

    private BigDecimal initialBalanceForTier(String tier) {
        return "PREMIUM".equals(tier) ? new BigDecimal("1000.00") : new BigDecimal("200.00");
    }
}
