package com.corefit.service.auth;

import com.corefit.dto.request.wallet.DepositRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.WalletTransactionResponse;
import com.corefit.entity.auth.User;
import com.corefit.entity.auth.WalletTransaction;
import com.corefit.entity.wallet.WalletPayment;
import com.corefit.enums.PaymentStatus;
import com.corefit.enums.WalletTransactionType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.auth.UserRepo;
import com.corefit.repository.auth.WalletPaymentRepo;
import com.corefit.repository.auth.WalletTransactionRepo;
import com.corefit.service.helper.NotificationService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WalletService {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private WalletTransactionRepo walletTransactionRepo;

    @Autowired
    private WalletPaymentRepo walletPaymentRepo;

    @Autowired
    private NotificationService notificationService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${production.url}")
    private String productionUrl;

    public GeneralResponse<?> getWallet(HttpServletRequest request) {
        User user = authService.extractUserFromRequest(request);
        return new GeneralResponse<>("success", user.getWallet());
    }

    public GeneralResponse<?> getTransactions(Integer page, Integer size, HttpServletRequest request) {
        User user = authService.extractUserFromRequest(request);
        Pageable pageable = PageRequest.of(
                page != null && page >= 1 ? page - 1 : 0,
                size != null && size > 0 ? size : 5,
                Sort.by("timestamp").descending()
        );

        Page<WalletTransaction> transactions = walletTransactionRepo.findAllByUserOrderByTimestampDesc(user, pageable);

        Page<WalletTransactionResponse> responsePage = transactions.map(tx -> WalletTransactionResponse.builder()
                .id(tx.getId())
                .userId(user.getId())
                .userName(user.getUsername())
                .type(tx.getType().name())
                .amount(tx.getAmount())
                .purpose(tx.getPurpose())
                .timestamp(tx.getTimestamp())
                .build());

        Map<String, Object> data = new HashMap<>();
        data.put("transactions", responsePage.getContent());
        data.put("currentPage", responsePage.getNumber() + 1);
        data.put("totalPages", responsePage.getTotalPages());
        data.put("totalElements", responsePage.getTotalElements());
        data.put("pageSize", responsePage.getSize());

        return new GeneralResponse<>("Wallet transactions fetched successfully", data);
    }

    public void withdraw(HttpServletRequest request, double value, String purpose) {
        User user = authService.extractUserFromRequest(request);
        if (user.getWallet() < value) {
            throw new GeneralException("Not enough money");
        }

        user.setWallet(user.getWallet() - value);
        userRepo.save(user);

        saveTransaction(user, value, WalletTransactionType.WITHDRAW, purpose);

        notificationService.pushNotification(user, "💸 Wallet Withdraw",
                String.format("An amount of %.2f EGP has been withdrawn from your wallet.", value));
    }

    public void deposit(long userId, double value, String purpose) {
        User user = authService.findUserById(userId);
        user.setWallet(user.getWallet() + value);
        userRepo.save(user);

        saveTransaction(user, value, WalletTransactionType.DEPOSIT, purpose);

        notificationService.pushNotification(user, "💰 Wallet Deposit",
                String.format("An amount of %.2f EGP has been added to your wallet.", value));
    }

    public void transfer(User fromUser, User toUser, double amount, String withdrawPurpose, String depositPurpose) {
        if (fromUser.getWallet() < amount) {
            throw new GeneralException("Not enough balance");
        }

        fromUser.setWallet(fromUser.getWallet() - amount);
        toUser.setWallet(toUser.getWallet() + amount);
        userRepo.saveAll(List.of(fromUser, toUser));

        saveTransaction(fromUser, amount, WalletTransactionType.WITHDRAW, withdrawPurpose);
        saveTransaction(toUser, amount, WalletTransactionType.DEPOSIT, depositPurpose);

        notificationService.pushNotification(fromUser, "💸 Wallet Withdraw",
                String.format("An amount of %.2f EGP has been withdrawn from your wallet.", amount));

        notificationService.pushNotification(toUser, "💰 Wallet Deposit",
                String.format("An amount of %.2f EGP has been added to your wallet.", amount));
    }

    public ResponseEntity<String> depositStripe(DepositRequest depositRequest, HttpServletRequest httpRequest) {
        try {
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

        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Stripe error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    public ResponseEntity<String> handleStripeSuccess(String sessionId) {
        try {
            Stripe.apiKey = stripeSecretKey;

            Optional<WalletPayment> existing = walletPaymentRepo.findBySessionId(sessionId);
            if (existing.isEmpty()) {
                return ResponseEntity.status(422).body("Session not found or used before");
            }

            WalletPayment payment = existing.get();

            if (payment.getStatus() == PaymentStatus.PAID) {
                return ResponseEntity.badRequest().body("This payment has already been processed");
            }

            Session session = Session.retrieve(sessionId);

            if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                long amountInCents = session.getAmountTotal();
                double amount = amountInCents / 100.0;

                deposit(payment.getUser().getId(), amount, "Stripe Payment deposit amount " + amount + " EGP");

                payment.setStatus(PaymentStatus.PAID);
                walletPaymentRepo.save(payment);

                return ResponseEntity.ok("Deposit successful");
            }

            return ResponseEntity.status(400).body("Payment not completed");

        } catch (StripeException e) {
            return ResponseEntity.status(500).body("Stripe error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    private void saveTransaction(User user, double amount, WalletTransactionType type, String purpose) {
        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .amount(amount)
                .type(type)
                .purpose(purpose)
                .timestamp(LocalDateTime.now())
                .build();
        walletTransactionRepo.save(transaction);
    }
}
