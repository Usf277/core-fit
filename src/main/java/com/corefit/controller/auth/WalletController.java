package com.corefit.controller.auth;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.auth.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {
    @Autowired
    private WalletService walletService;

    @GetMapping("/wallet")
    public ResponseEntity<GeneralResponse<?>> getWallet(HttpServletRequest request) {
        GeneralResponse<?> response = walletService.getWallet(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/transactions")
    public ResponseEntity<GeneralResponse<?>> getWalletTransactions(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request) {
        GeneralResponse<?> response = walletService.getTransactions(page, size, request);
        return ResponseEntity.ok(response);
    }

}
