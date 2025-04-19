package com.corefit.dto.request.market;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductRequest {
    private long id;
    private String name;
    private String description;
    private double price;
    private int offer;
    private Long marketId;
    private Long subCategoryId;
    private boolean isHidden;
    private List<String> imagesToKeep;
}
