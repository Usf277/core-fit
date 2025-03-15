package com.corefit.dto.response.market;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private long id;
    private Long marketId;
    private List<CartItemResponse> products;
    private double totalPrice;
}
