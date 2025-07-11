package com.corefit.controller.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.helper.ProviderStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderStatistics {

    @Autowired
    private ProviderStatisticsService providerStatisticsService;

    @GetMapping("provider_statistics")
    public ResponseEntity<GeneralResponse<?>> getProviderStats(HttpServletRequest httpRequest) {
        GeneralResponse<?> response = providerStatisticsService.getProviderStats(httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
