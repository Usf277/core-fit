package com.corefit.dto.request.playground;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaygroundRequest {
    private long id;
    private String name;
    private String description;
    private long cityId;
    private String address;
    private String morningShiftStart;
    private String morningShiftEnd;
    private String nightShiftStart;
    private String nightShiftEnd;
    private double bookingPrice;
    private boolean hasExtraPrice;
    private double extraNightPrice;
}
