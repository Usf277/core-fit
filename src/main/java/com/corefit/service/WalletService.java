package com.corefit.service;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.UserRepo;
import com.corefit.service.market.AuthService;
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
        User user = authService.extractUserFromRequest(request);
        return new GeneralResponse<>("success", user.getWallet());
    }

    public void withdraw(HttpServletRequest request, double value) {
        User user = authService.extractUserFromRequest(request);

        if (user.getWallet() < value) {
            throw new GeneralException("Not enough money");
        }
        user.setWallet(user.getWallet() - value);

        userRepo.save(user);
    }

    public void deposit(long userId, double value) {
        User user = authService.findUserById(userId);
        user.setWallet(user.getWallet() + value);
        userRepo.save(user);
    }
}
