package com.corefit.controller;

import com.corefit.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CitiesController {
    @Autowired
    private CityService cityService;

    @GetMapping("/cities")
    public ResponseEntity<?> getCitiesByGovernorateId(@RequestParam long governorate_id) {
        return ResponseEntity.ok(cityService.getAllCities(governorate_id));
    }
}
