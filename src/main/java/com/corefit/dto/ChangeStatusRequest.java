package com.corefit.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangeStatusRequest {
    private Long orderId;
    private String status;
}
