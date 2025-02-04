package com.corefit.service;

import com.corefit.entity.City;
import com.corefit.repository.CityRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {
    private final CityRepo cityRepo;

    public CityService(CityRepo cityRepo) {
        this.cityRepo = cityRepo;
    }


    public String createCity(City city) {
        cityRepo.save(city);
        return "City Saved Successfully";
    }

    public City findById(long id) {
        return cityRepo.findById(id);
    }


    public List<City> getAllCities() {
        return cityRepo.findAll();
    }
}
