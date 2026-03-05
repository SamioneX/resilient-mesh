package com.resilientmesh.wallet;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
class WalletController {

    private final WalletDomainService walletDomainService;

    WalletController(WalletDomainService walletDomainService) {
        this.walletDomainService = walletDomainService;
    }

    @GetMapping("/{userId}")
    WalletResponse wallet(@PathVariable String userId) {
        return walletDomainService.fetchWallet(userId);
    }

    @PostMapping("/{userId}/debit")
    ResponseEntity<DebitResult> debit(
            @PathVariable String userId,
            @Valid @RequestBody DebitRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(walletDomainService.debit(userId, request.amount(), idempotencyKey));
    }
}
