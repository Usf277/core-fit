package com.corefit.dto.request.playground;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaygroundRequest {
    private long id;
    private String name;
    private String description;
    private long cityId;
    private String address;
    private String lat;
    private String lng;
    private String morningShiftStart;
    private String morningShiftEnd;
    private String nightShiftStart;
    private String nightShiftEnd;
    private double bookingPrice;
    private Integer teamMembers;
    private boolean hasExtraPrice;
    private double extraNightPrice;
    private List<String> images;
    private Integer password;
    private boolean passwordEnabled;
}
