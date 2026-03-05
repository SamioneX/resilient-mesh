package com.resilientmesh.wallet;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);
}
