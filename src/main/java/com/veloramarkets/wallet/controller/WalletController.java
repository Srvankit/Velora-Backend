package com.veloramarkets.wallet.controller;

import com.veloramarkets.wallet.dto.WalletAmountRequest;
import com.veloramarkets.wallet.dto.WalletResponse;
import com.veloramarkets.wallet.dto.WalletTransactionResponse;
import com.veloramarkets.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                walletService.getWallet(authentication)
        );
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(
            Authentication authentication,
            @Valid @RequestBody WalletAmountRequest request
    ) {
        return ResponseEntity.ok(
                walletService.deposit(
                        authentication,
                        request.amount()
                )
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdraw(
            Authentication authentication,
            @Valid @RequestBody WalletAmountRequest request
    ) {
        return ResponseEntity.ok(
                walletService.withdraw(
                        authentication,
                        request.amount()
                )
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionResponse>>
    getTransactions(Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getTransactions(authentication)
        );
    }
}