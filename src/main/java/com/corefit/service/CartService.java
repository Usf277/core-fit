package com.corefit.service;

import com.corefit.dto.GeneralResponse;
import com.corefit.entity.Cart;
import com.corefit.entity.CartItem;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.CartItemRepo;
import com.corefit.repository.CartRepo;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CartItemRepo cartItemRepo;

    public GeneralResponse<?> getCart(HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        Long userIdLong = Long.parseLong(userId);

        User user = userRepo.findById(userIdLong)
                .orElseThrow(() -> new GeneralException("User not found"));

        Cart cart = cartRepo.findByUserId(userIdLong)
                .orElseThrow(() -> new GeneralException("Cart not found"));


        Map<String, Object> data = new HashMap<>();
        data.put("cart", cart);

        return new GeneralResponse<>("Success", data);
    }
}
