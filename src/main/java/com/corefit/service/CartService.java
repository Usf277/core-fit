package com.corefit.service;

import com.corefit.dto.CartDto;
import com.corefit.dto.CartItemDto;
import com.corefit.dto.GeneralResponse;
import com.corefit.entity.Cart;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.CartItemRepo;
import com.corefit.repository.CartRepo;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepo userRepo;

    public GeneralResponse<?> getCart(HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        Long userIdLong = Long.parseLong(userId);

        User user = userRepo.findById(userIdLong)
                .orElseThrow(() -> new GeneralException("User not found"));

        Cart cart = cartRepo.findByUserId(userIdLong);

        if (cart == null) {
            Cart newCart = new Cart();
            cart.setUser(user);
            cart = cartRepo.save(newCart);
        }

        List<CartItemDto> cartItemDtos = cart.getCartItems().stream()
                .map(item -> new CartItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getDescription(),
                        item.getProduct().getPrice(),
                        item.getProduct().getOffer(),
                        item.getProduct().getSubCategory().getName(),
                        item.getProduct().getImages(),
                        item.getQuantity()
                )).toList();

        double totalPrice = cartItemDtos.stream().mapToDouble(CartItemDto::getTotal).sum();

        CartDto cartDto = new CartDto(
                cart.getId(),
                cart.getMarket() != null ? cart.getMarket().getId() : null,
                cartItemDtos,
                totalPrice);

        return new GeneralResponse<>("Success", cartDto);
    }

//    private GeneralResponse<?> insertCart(Cart cart) {
//        return new GeneralResponse<>("cart added successfully", cart);
//    }
}
