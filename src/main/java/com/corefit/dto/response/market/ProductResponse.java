package com.corefit.dto.response.market;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductResponse {
    private long id;
    private String name;
    private String description;
    private double price;
    private int offer;
    private String marketName;
    private Long marketId;
    private String subCategoryName;
    private List<String> images;
    private boolean isHidden;
    private boolean isFavourite;
}
