package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.MarketRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.MarketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/find_market")
    public ResponseEntity<GeneralResponse<?>> getMarket(@RequestParam long id) {
        try {
            GeneralResponse<?> response = marketService.findById(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/markets")
    public ResponseEntity<?> findAll(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(marketService.getAll(page, size));
    }

    @PostMapping(value = "/add_market", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> addMarket(@ModelAttribute MarketRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(marketService.insert(request, httpRequest));
    }

    @PostMapping(value = "/edit_market", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> editMarket(@ModelAttribute MarketRequest request, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = marketService.update(request, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/delete_market")
    public ResponseEntity<GeneralResponse<?>> deleteMarket(@RequestParam long id, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = marketService.delete(id, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/change_status")
    public ResponseEntity<GeneralResponse<?>> changeStatus(@RequestParam long id, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = marketService.changeStatus(id, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
