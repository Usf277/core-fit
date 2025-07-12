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
    public ResponseEntity<?> createDeposit(@RequestBody DepositRequest depositRequest, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(walletService.depositStripe(depositRequest, httpRequest));
    }

    @GetMapping("/wallet/deposit/success")
    public ResponseEntity<?> handleSuccess(@RequestParam("session_id") String sessionId) {
        return ResponseEntity.ok(walletService.handleStripeSuccess(sessionId));
    }

    @PostMapping("/wallet/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody DepositRequest depositRequest, HttpServletRequest request) {
        walletService.withdraw(request, depositRequest.getAmount(), "Stripe withdraw transaction");
        return ResponseEntity.ok("Withdrawal successful");
    }
}
