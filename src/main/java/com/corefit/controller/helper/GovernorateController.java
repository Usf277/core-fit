package com.corefit.controller.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.GovernorateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GovernorateController {
    @Autowired
    private GovernorateService governorateService;

    @GetMapping("/governorates")
    public ResponseEntity<?> getAll() {
        try {
            GeneralResponse<?> response = governorateService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}