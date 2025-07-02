package com.corefit.service.auth;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.WalletTransactionResponse;
import com.corefit.entity.auth.User;
import com.corefit.entity.auth.WalletTransaction;
import com.corefit.enums.WalletTransactionType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.auth.UserRepo;
import com.corefit.repository.auth.WalletTransactionRepo;
import com.corefit.service.helper.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private WalletTransactionRepo walletTransactionRepo;
    @Autowired
    private NotificationService notificationService;

    public GeneralResponse<?> getWallet(HttpServletRequest request) {
        User user = authService.extractUserFromRequest(request);
        return new GeneralResponse<>("success", user.getWallet());
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

    public GeneralResponse<?> getTransactions(Integer page, Integer size, HttpServletRequest request) {
        User user = authService.extractUserFromRequest(request);

        Pageable pageable = PageRequest.of(page != null && page >= 1 ? page - 1 : 0, size != null && size > 0 ? size : 5, Sort.by("timestamp").descending());

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

    /// Helper method
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