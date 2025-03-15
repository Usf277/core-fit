package com.corefit.dto.response.market;

import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderItemResponse {
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
