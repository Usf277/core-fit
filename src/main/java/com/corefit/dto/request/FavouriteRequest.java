package com.corefit.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavouriteRequest {
    private long productId;
    private String type;
}
