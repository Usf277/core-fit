package com.corefit.dto.response.playground;

import com.corefit.entity.helper.City;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaygroundResponse {
    private Long id;
    private String name;
    private String description;
    private City city;
    private String address;
    private String lat;
    private String lng;

    private String morningShiftStart;
    private String morningShiftEnd;
    private String nightShiftStart;
    private String nightShiftEnd;

    private Double bookingPrice;
    private boolean hasExtraPrice;
    private double extraNightPrice;
    private Integer teamMembers;

    private boolean passwordEnabled;
    private boolean isOpened;
    private boolean isFavourite;

    private List<String> images;
    private int avgRate;
    private long rateCount;
}
