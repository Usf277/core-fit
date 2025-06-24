package com.corefit.service.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.helper.City;
import com.corefit.repository.helper.CityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CityService {
    @Autowired
    private CityRepo cityRepo;

    public City findById(long id) {
        return cityRepo.findById(id);
    }

    public GeneralResponse<?> getAllCities(Long governorateId) {
        List<City> cities;

        if (governorateId != null)
            cities = cityRepo.findAllByGovernorateId(governorateId);
        else
            cities = cityRepo.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("cities", cities);

        return new GeneralResponse<>("Cities retrieved successfully", data);
    }

}
