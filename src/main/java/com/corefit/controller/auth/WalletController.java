package com.corefit.controller.auth;

import com.corefit.dto.request.wallet.DepositRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.auth.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/wallet")
    public ResponseEntity<GeneralResponse<?>> getWallet(HttpServletRequest request) {
        return ResponseEntity.ok(walletService.getWallet(request));
    }

    @GetMapping("/wallet/transactions")
    public ResponseEntity<GeneralResponse<?>> getWalletTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(walletService.getTransactions(page, size, request));
    }

    @PostMapping("/wallet/deposit")
    public ResponseEntity<String> createDeposit(@RequestBody DepositRequest depositRequest,
                                                HttpServletRequest httpRequest) {
        return walletService.depositStripe(depositRequest, httpRequest);
    }

    @GetMapping("/wallet/deposit/success")
    public ResponseEntity<String> handleSuccess(@RequestParam("session_id") String sessionId) {
        return walletService.handleStripeSuccess(sessionId);
    }
}
