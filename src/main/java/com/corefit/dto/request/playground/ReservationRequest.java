package com.corefit.dto.request.playground;

import com.corefit.entity.playground.ReservationSlot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReservationRequest {
    private Long playgroundId;
    private LocalDate date;
    private List<ReservationSlot> slots = new ArrayList<>();
}
