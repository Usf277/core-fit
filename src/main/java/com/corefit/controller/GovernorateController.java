package com.corefit.controller;

import com.corefit.entity.Governorate;
import com.corefit.service.GovernorateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GovernorateController {

    @Autowired
    private GovernorateService governorateService;

    @PostMapping("/governorates")
    public ResponseEntity<?> insert(@RequestBody Governorate governorate) {
        return ResponseEntity.ok(governorateService.insert(governorate));
    }

    @GetMapping("/governorates")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(governorateService.getAll());
    }
}