package com.corefit.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
