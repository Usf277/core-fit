package com.corefit.service;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.City;
import com.corefit.repository.CityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CityService {
    @Autowired
    private CityRepo cityRepo;

    public City findById(long id) {
        return cityRepo.findById(id);
    }

    public GeneralResponse<?> getAllCities(long governorate_id) {
        Map<String, Object> data = new HashMap<>();
        data.put("cities", cityRepo.findAllByGovernorateId(governorate_id));
        return new GeneralResponse<>("Success", data);
    }
}
