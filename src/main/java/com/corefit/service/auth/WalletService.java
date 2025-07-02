package com.corefit.service.auth;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.WalletTransactionResponse;
import com.corefit.entity.auth.User;
import com.corefit.entity.auth.WalletTransaction;
import com.corefit.enums.WalletTransactionType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.auth.UserRepo;
import com.corefit.repository.auth.WalletTransactionRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
public class WalletService {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private WalletTransactionRepo walletTransactionRepo;

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
    }

    public void deposit(long userId, double value, String purpose) {
        User user = authService.findUserById(userId);
        user.setWallet(user.getWallet() + value);
        userRepo.save(user);

        saveTransaction(user, value, WalletTransactionType.DEPOSIT, purpose);
    }

    public GeneralResponse<?> getTransactions(Integer page, Integer size, HttpServletRequest request) {
        User user = authService.extractUserFromRequest(request);

        Pageable pageable = PageRequest.of(page != null && page >= 1 ? page - 1 : 0, size != null && size > 0 ? size : 5, Sort.by("id").ascending());
        Page<WalletTransaction> transactions = walletTransactionRepo.findAllByUserOrderByTimestampDesc(user, pageable);

        Page<WalletTransactionResponse> responsePage = transactions.map(tx -> WalletTransactionResponse.builder()
                .id(tx.getId())
                .userId(user.getId())
                .userName(user.getUsername())
                .type(tx.getType().name())
                .amount(tx.getAmount())
                .purpose(tx.getPurpose())
                .timestamp(tx.getTimestamp())
                .build()
        );

        return new GeneralResponse<>("Wallet transactions fetched successfully", responsePage);
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
