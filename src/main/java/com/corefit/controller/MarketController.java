package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.MarketRequest;
import com.corefit.service.MarketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/markets")
    public ResponseEntity<GeneralResponse<?>> findAll() {
        return ResponseEntity.ok(marketService.getAll());
    }

    @PostMapping(value = "/add_market", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> addMarket(@ModelAttribute MarketRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(marketService.insert(request, httpRequest));
    }

    @PostMapping(value = "/edit_market", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> editMarket(@ModelAttribute MarketRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(marketService.update(request, httpRequest));
    }
}
