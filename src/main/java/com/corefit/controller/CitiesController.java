package com.corefit.controller;

import com.corefit.entity.City;
import com.corefit.service.CityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CitiesController {
    private final CityService cityService;

    public CitiesController(CityService cityService) {
        this.cityService = cityService;
    }


    @GetMapping("/cities")
    public List<City> getCities() {
        return cityService.getAllCities();
    }


    @PostMapping("/cities")
    public String addCity(@RequestBody City city) {
        return cityService.createCity(city);
    }
}
