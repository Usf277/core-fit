package com.corefit.dto.response.playground;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
public class ReservationResponse {
    private Long id;
    private Long userId;
    private LocalDate date;
    private List<String> slots;
    private double price;
    private boolean cancelled;
    private LocalDateTime createdAt;


    private Long playgroundId;
    private String playgroundName;
    private String playgroundAddress;
    private int playgroundAvgRate;
    private long numberPlaygroundRates;
    private int teamMembers;
}
