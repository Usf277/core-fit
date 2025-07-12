package com.corefit.controller.auth;

import com.corefit.dto.request.wallet.DepositRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.auth.User;
import com.corefit.entity.wallet.WalletPayment;
import com.corefit.enums.PaymentStatus;
import com.corefit.repository.auth.WalletPaymentRepo;
import com.corefit.service.auth.AuthService;
import com.corefit.service.auth.WalletService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private AuthService authService;

    @Autowired
    private WalletPaymentRepo walletPaymentRepo;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${production.url}")
    private String productionUrl;

    @GetMapping("/wallet")
    public ResponseEntity<GeneralResponse<?>> getWallet(HttpServletRequest request) {
        GeneralResponse<?> response = walletService.getWallet(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/transactions")
    public ResponseEntity<GeneralResponse<?>> getWalletTransactions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        GeneralResponse<?> response = walletService.getTransactions(page, size, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wallet/deposit")
    public ResponseEntity<String> createDeposit(@RequestBody DepositRequest depositRequest,
                                                HttpServletRequest httpRequest) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        User user = authService.extractUserFromRequest(httpRequest);

        if (depositRequest.getAmount() == null || depositRequest.getAmount() <= 0) {
            return ResponseEntity.badRequest().body("Invalid deposit amount");
        }

        BigDecimal amount = BigDecimal.valueOf(depositRequest.getAmount()).setScale(2, RoundingMode.HALF_UP);
        long stripeAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(productionUrl + "/wallet/deposit/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(productionUrl + "/wallet/deposit/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("egp")
                                                .setUnitAmount(stripeAmount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Wallet Deposit")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("userId", String.valueOf(user.getId()))
                .putMetadata("username", user.getUsername())
                .putMetadata("email", user.getEmail())
                .build();

        Session session = Session.create(params);


        WalletPayment payment = WalletPayment.builder()
                .user(user)
                .sessionId(session.getId())
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        walletPaymentRepo.save(payment);

        return ResponseEntity.ok(session.getUrl());
    }

    @GetMapping("/wallet/deposit/success")
    public ResponseEntity<String> handleSuccess(@RequestParam("session_id") String sessionId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        Optional<WalletPayment> existing = walletPaymentRepo.findBySessionId(sessionId);

        if (existing.isPresent()) {
            WalletPayment payment = existing.get();

            if (payment.getStatus() == PaymentStatus.PAID) {
                return ResponseEntity.badRequest().body("This payment has already been processed");
            }

            Session session = Session.retrieve(sessionId);
            if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                long amountInCents = session.getAmountTotal();
                double amount = amountInCents / 100.0;

                walletService.deposit(payment.getUser().getId(), amount, "Stripe Payment deposit amount " + amount + " EGP");

                payment.setStatus(PaymentStatus.PAID);
                walletPaymentRepo.save(payment);

                return ResponseEntity.ok("Deposit successful");
            }

            return ResponseEntity.status(400).body("Payment not completed");
        }

        return ResponseEntity.status(422).body("Session not found or used before");
    }
}
