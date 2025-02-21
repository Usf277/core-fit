package com.corefit.service;

import com.corefit.dto.*;
import com.corefit.entity.*;
import com.corefit.enums.OrderStatus;
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
import java.util.stream.Collectors;

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
        long userId = Long.parseLong(authService.extractUserIdFromRequest(httpRequest));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new GeneralException("User not found"));

        Cart cart = cartRepo.findByUserId(user.getId());

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new GeneralException("Cart is empty. Add items before placing an order.");
        }

        List<CartItem> cartItems = cart.getCartItems();
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();

        order.setUser(user);
        order.setLongitude(orderRequest.getLongitude());
        order.setLatitude(orderRequest.getLatitude());
        order.setClientAddress(orderRequest.getClientAddress());
        order.setClientName(orderRequest.getClientName());
        order.setClientPhone(orderRequest.getClientPhone());
        order.setAdditionalInfo(orderRequest.getAdditionalInfo());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setMarket(cart.getMarket());
        order.setTotalPrice(cart.getTotalPrice());

        Order finalOrder = order;
        cartItems.forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrder(finalOrder);
            orderItem.setTotal(cartItem.getTotal());
            orderItems.add(orderItem);
        });
        order.setOrderItems(orderItems);

        if (orderRequest.getPaymentMethod() == PaymentMethod.WALLET) {
            try {
                walletService.withdraw(httpRequest, cart.getTotalPrice());
            } catch (Exception e) {
                throw new GeneralException("Wallet payment failed: " + e.getMessage());
            }
        }

        order = orderRepo.save(order);
        cartService.deleteCart(httpRequest);

        return new GeneralResponse<>("Order created successfully", mapToOrderResponse(order));
    }

    public GeneralResponse<?> getOrder(Long orderId, HttpServletRequest httpRequest) {
        long userId = Long.parseLong(authService.extractUserIdFromRequest(httpRequest));
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new GeneralException("Order not found"));

        if (order.getUser().getId() != userId) {
            throw new GeneralException("User not authorized to order");
        }

        return new GeneralResponse<>("Success", mapToOrderResponse(order));
    }

    public GeneralResponse<?> getOrders(String status, HttpServletRequest httpRequest) {
        long userId = Long.parseLong(authService.extractUserIdFromRequest(httpRequest));

        if (status == null || status.trim().isEmpty()) {
            status = "current";
        }

        List<Order> orders;
        if ("previous".equalsIgnoreCase(status)) {
            orders = orderRepo.findPreviousOrdersByUserId(userId);
        } else {
            orders = orderRepo.findActiveOrdersByUserId(userId);
        }

        List<OrderResponse> orderResponses = orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
        return new GeneralResponse<>("Success", orderResponses);
    }

    @Transactional
    public GeneralResponse<?> cancelOrder(long orderId, HttpServletRequest httpRequest) {
        long userId = Long.parseLong(authService.extractUserIdFromRequest(httpRequest));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new GeneralException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new GeneralException("User not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.ORDER_DELIVERED || order.getStatus() == OrderStatus.ORDER_UNDER_DELIVERY) {
            throw new GeneralException("Order has already been delivered and cannot be canceled");
        } else if (order.getStatus() == OrderStatus.ORDER_CANCELED) {
            throw new GeneralException("Order is already canceled");
        }

        if (order.getPaymentMethod() == PaymentMethod.WALLET) {
            try {
                walletService.deposit(httpRequest, order.getTotalPrice());
            } catch (Exception e) {
                throw new GeneralException("Refund failed: " + e.getMessage());
            }
        }

        order.setStatus(OrderStatus.ORDER_CANCELED);
        orderRepo.save(order);

        return new GeneralResponse<>("Order canceled successfully");
    }


    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getDescription(),
                        item.getProduct().getPrice(),
                        item.getProduct().getOffer(),
                        item.getProduct().getSubCategory().getName(),
                        item.getProduct().getImages(),
                        item.getQuantity(),
                        item.getTotal())).collect(Collectors.toList());

        return new OrderResponse(order.getId(), order.getUser(), order.getClientName(), order.getClientAddress(), order.getClientPhone()
                , order.getLatitude(), order.getLongitude(), order.getAdditionalInfo(), order.getStatus(), order.getPaymentMethod(),
                order.getTotalPrice(), order.getMarket(), orderItems);
    }

}
