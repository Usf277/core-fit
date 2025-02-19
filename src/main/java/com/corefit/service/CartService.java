package com.corefit.service;

import com.corefit.dto.CartDto;
import com.corefit.dto.CartItemDto;
import com.corefit.dto.GeneralResponse;
import com.corefit.entity.Cart;
import com.corefit.entity.CartItem;
import com.corefit.entity.Product;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.CartRepo;
import com.corefit.repository.ProductRepo;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProductRepo productRepo;

    @Transactional
    public GeneralResponse<?> getCart(HttpServletRequest httpRequest) {
        User user = getUserFromRequest(httpRequest);
        Cart cart = getOrCreateCartForUser(user);

        CartDto cartDto = mapToCartDto(cart);
        return new GeneralResponse<>("Success", cartDto);
    }

    @Transactional
    public GeneralResponse<?> addItemToCart(HttpServletRequest httpRequest, long productId, long quantity) {
        User user = getUserFromRequest(httpRequest);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new GeneralException("Product not found"));

        Cart cart = getOrCreateCartForUser(user);
        long marketId = product.getMarket().getId();

        if (cart.getMarket() == null || cart.getCartItems().isEmpty()) {
            cart.setMarket(product.getMarket());
        } else if (cart.getMarket().getId() != marketId) {
            throw new GeneralException("You can't add items from another market. Remove existing cart items first.");
        }

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            if (quantity == 0) {
                cart.getCartItems().remove(existingItem);
            } else {
                existingItem.setQuantity((int) quantity);
            }
        } else if (quantity > 0) {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity((int) quantity);
            newItem.setCart(cart);
            cart.getCartItems().add(newItem);
        }

        cartRepo.save(cart);
        CartDto cartDto = mapToCartDto(cart);
        return new GeneralResponse<>("Success", cartDto);
    }


    private User getUserFromRequest(HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        Long userIdLong = Long.parseLong(userId);
        return userRepo.findById(userIdLong)
                .orElseThrow(() -> new GeneralException("User not found"));
    }

    private Cart getOrCreateCartForUser(User user) {
        Cart cart = cartRepo.findByUserId(user.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepo.save(cart);
        }
        return cart;
    }

    private CartDto mapToCartDto(Cart cart) {
        List<CartItemDto> cartItems = cart.getCartItems().stream()
                .map(item -> new CartItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getDescription(),
                        item.getProduct().getPrice(),
                        item.getProduct().getOffer(),
                        item.getProduct().getSubCategory().getName(),
                        item.getProduct().getImages(),
                        item.getQuantity()
                )).collect(Collectors.toList());

        double totalPrice = cartItems.stream().mapToDouble(CartItemDto::getTotal).sum();

        return new CartDto(
                cart.getId(),
                cart.getMarket() != null ? cart.getMarket().getId() : null,
                cartItems,
                totalPrice);
    }
}
