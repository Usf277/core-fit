package com.corefit.controller.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.CityRepo;
import com.corefit.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CitiesController {
    @Autowired
    private CityService cityService;
    @Autowired
    private CityRepo cityRepo;

    @GetMapping("/cities")
    public ResponseEntity<?> getCitiesByGovernorateId(@RequestParam long governorate_id) {
        try {
            GeneralResponse<?> response = cityService.getAllCities(governorate_id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
