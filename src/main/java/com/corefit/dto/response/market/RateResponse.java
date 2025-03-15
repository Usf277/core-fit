package com.corefit.dto.response.market;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RateResponse {
    private Long id;
    private String comment;
    private int rate;
    private LocalDateTime createdAt;
    private String username;
}
