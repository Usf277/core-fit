package com.corefit.dto.response.market;

import com.corefit.enums.OrderStatus;
import com.corefit.enums.PaymentMethod;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private Long marketId;
    private LocalDateTime createdAt;
    private String marketName;

    private String clientName;
    private String clientAddress;
    private String clientPhone;
    private Double latitude;
    private Double longitude;
    private String additionalInfo;
    private PaymentMethod paymentMethod;

    private OrderStatus status;
    private Double totalPrice;
    private List<OrderItemResponse> orderItems;
}
