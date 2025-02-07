package com.corefit.controller;

import com.corefit.service.CityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CitiesController {
    private final CityService cityService;

    public CitiesController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("/cities")
    public ResponseEntity<?> getCitiesByGovernorateId(@RequestParam long governorate_id) {
        return ResponseEntity.ok(cityService.getAllCities(governorate_id));
    }
}
