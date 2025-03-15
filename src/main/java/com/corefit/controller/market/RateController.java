package com.corefit.controller.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.market.RateRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.RateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rates")
public class RateController {
    @Autowired
    private RateService rateService;

    @PostMapping(value = "/add_rate")
    public ResponseEntity<GeneralResponse<?>> addRate(@RequestBody RateRequest request, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = rateService.insert(request, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/find_by_market")
    public ResponseEntity<GeneralResponse<?>> getRatesByMarket(
            @RequestParam Long marketId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            GeneralResponse<?> response = rateService.getRatesByMarket(marketId, page, size);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
