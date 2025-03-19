package com.corefit.dto.request.playground;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaygroundRequest {
    private long id;
    private String name;
    private String description;
    private String lat;
    private String lng;
    private String address;
    private int teamMembers;
    private String morningShiftStart;
    private String morningShiftEnd;
    private String nightShiftStart;
    private String nightShiftEnd;
    private double bookingPrice;
    private boolean hasExtraPrice;
    private double extraNightPrice;
}
