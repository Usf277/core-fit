package com.corefit.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class CartItemDto {
    private long id;
    private String name;
    private String description;
    private double price;
    private int offer;
    private String subCategoryName;
    private List<String> images;
    private int count;
    private double total;

    public CartItemDto(long id, String name, String description, double price, int offer, String subCategoryName, List<String> images, int count) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.offer = offer;
        this.subCategoryName = subCategoryName;
        this.images = images;
        this.count = count;
        this.total = calculateTotal();
    }

    private double calculateTotal() {
        if (offer == 0)
            return count * price;
        else
            return count * (price - (price * offer / 100));
    }
}
