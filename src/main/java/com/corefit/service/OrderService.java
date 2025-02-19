package com.corefit.service;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.OrderRequest;
import com.corefit.entity.*;
import com.corefit.enums.PaymentMethod;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.CartRepo;
import com.corefit.repository.OrderRepo;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private CartService cartService;
    @Autowired
    private WalletService walletService;

    @Transactional
    public GeneralResponse<?> createOrder(OrderRequest orderRequest, HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        Long userIdLong = Long.parseLong(userId);

        User user = userRepo.findById(userIdLong)
                .orElseThrow(() -> new GeneralException("User not found"));

        Cart cart = cartRepo.findByUserId(user.getId());

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new GeneralException("Cart is empty. Add items before placing an order.");
        }

        List<CartItem> cartItems = cart.getCartItems();
        List<OrderItem> orderItems = new ArrayList<>();

        double totalPrice = cartItems.stream()
                .mapToDouble(cartItem -> (cartItem.getProduct().getPrice() * cartItem.getQuantity() * (100 - cartItem.getProduct().getOffer()) / 100))
                .sum();

        Order order = new Order();
        order.setUser(user);
        order.setLongitude(orderRequest.getLongitude());
        order.setLatitude(orderRequest.getLatitude());
        order.setClientAddress(orderRequest.getClientAddress());
        order.setClientName(orderRequest.getClientName());
        order.setClientPhone(orderRequest.getClientPhone());
        order.setAdditionalInfo(orderRequest.getAdditionalInfo());
        order.setMarket(cart.getMarket());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setTotalPrice(totalPrice);

        Order finalOrder = order;

        cartItems.forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrder(finalOrder);
            orderItems.add(orderItem);
        });

        order.setOrderItems(orderItems);

        if (orderRequest.getPaymentMethod() == PaymentMethod.WALLET) {
            try {
                walletService.withdraw(httpRequest, totalPrice);
            } catch (Exception e) {
                throw new GeneralException("Wallet payment failed: " + e.getMessage());
            }
        }

        order = orderRepo.save(order);

        cartService.deleteCart(httpRequest);

        return new GeneralResponse<>("Order created successfully", order);
    }

}
