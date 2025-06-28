package com.corefit.dto.response.playground;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Builder
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long playgroundId;
    private LocalDate date;
    private List<String> slots;
    private double price;
    private boolean cancelled;
}
