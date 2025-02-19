package com.corefit.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private long id;
    private Long marketId;
    private List<CartItemDto> products;
    private double totalPrice;
}
