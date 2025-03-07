package com.corefit.controller;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.RateRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rates")
public class RateController {
    @Autowired
    private RateService rateService;

    @GetMapping("/find_by_market")
    public ResponseEntity<GeneralResponse<?>> getRatesByMarket(
            @RequestParam Long marketId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(rateService.getRatesByMarket(marketId, page, size));
    }

    @PostMapping(value = "/add_rate")
    public ResponseEntity<GeneralResponse<?>> addRate(@RequestBody RateRequest request) {

        try {
            GeneralResponse<?> response = rateService.insert(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

}
