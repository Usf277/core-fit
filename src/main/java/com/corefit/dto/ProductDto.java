package com.corefit.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductDto {
    private long id;
    private String name;
    private String description;
    private double price;
    private int offer;
    private String marketName;
    private String subCategoryName;
    private List<String> images;
    private boolean isHidden;
}
