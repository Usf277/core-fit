package com.corefit.service;

import com.corefit.dto.GeneralResponse;
import com.corefit.entity.City;
import com.corefit.repository.CityRepo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CityService {
    private final CityRepo cityRepo;

    public CityService(CityRepo cityRepo) {
        this.cityRepo = cityRepo;
    }

    public City findById(long id) {
        return cityRepo.findById(id);
    }

    public GeneralResponse<?> getAllCities(long governorate_id) {
        Map<String, Object> data = new HashMap<>();
        data.put("cities", cityRepo.findAllByGovernorateId(governorate_id));
        return new GeneralResponse<>("Success", data);
    }
}
