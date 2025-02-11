package com.corefit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    private long id;
    private String name;
    private String description;
    private double price;
    private int offer;
    private boolean isHidden;
    private Long marketId;
    private Long subCategoryId;
}
