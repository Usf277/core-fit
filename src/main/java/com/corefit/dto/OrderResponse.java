package com.corefit.dto;

import com.corefit.entity.Market;
import com.corefit.entity.OrderItem;
import com.corefit.entity.User;
import com.corefit.enums.OrderStatus;
import com.corefit.enums.PaymentMethod;
import com.corefit.repository.OrderItemRepo;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private User user;
    private String clientName;
    private String clientAddress;
    private String clientPhone;
    private Double latitude;
    private Double longitude;
    private String additionalInfo;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private Double totalPrice;
    private Market market;
    private List<OrderItemResponse> orderItems ;

}
