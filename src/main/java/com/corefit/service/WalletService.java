package com.corefit.service;

import com.corefit.dto.GeneralResponse;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepo userRepo;

    public GeneralResponse<?> getWallet(HttpServletRequest request) {
        String userId = authService.extractUserIdFromRequest(request);
        Long userIdLong = Long.parseLong(userId);

        User user = userRepo.findById(userIdLong)
                .orElseThrow(() -> new GeneralException("User not found"));

        return new GeneralResponse<>("success", user.getWallet());
    }

    public void withdraw(HttpServletRequest request, double value) {
        long userId = Long.parseLong(authService.extractUserIdFromRequest(request));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new GeneralException("User not found"));

        if(user.getWallet() < value) {
            throw new GeneralException("Not enough money");
        }
        user.setWallet(user.getWallet() - value);

        userRepo.save(user);
    }

    public void deposit(long userId, double value) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new GeneralException("User not found"));

        user.setWallet(user.getWallet() + value);

        userRepo.save(user);
    }
}
