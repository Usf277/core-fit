package com.corefit.dto.request.market;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateRequest {
    private long id;
    private String comment;
    private Integer rate; // 0 : 5
    private Long marketId;
}
