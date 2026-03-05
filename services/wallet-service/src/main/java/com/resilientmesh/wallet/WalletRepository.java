package com.resilientmesh.wallet;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(String userId);
}
