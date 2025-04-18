package com.corefit.dto.response.market;

import com.corefit.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrdersResponse {
    private Long id;
    private Long userId;
    private Long marketId;
    private LocalDateTime createdAt;
    private String marketName;
    private String clientAddress;
    private OrderStatus status;
    private Double totalPrice;
}
