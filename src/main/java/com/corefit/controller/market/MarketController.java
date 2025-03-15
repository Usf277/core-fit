package com.corefit.controller.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.market.MarketRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.MarketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;

    @GetMapping("/find_market")
    public ResponseEntity<GeneralResponse<?>> getMarket(@RequestParam long id) {
        try {
            GeneralResponse<?> response = marketService.getMarketById(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/markets")
    public ResponseEntity<GeneralResponse<?>> findAll(@RequestParam int page, @RequestParam(required = false) Integer size
            , @RequestParam(required = false) String search, @RequestParam(required = false) Long categoryId) {
        try {
            GeneralResponse<?> response = marketService.getAll(page, size, search, categoryId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/add_market", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> addMarket(@ModelAttribute MarketRequest request, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = marketService.insert(request, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/edit_market", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> editMarket(@ModelAttribute MarketRequest request, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = marketService.update(request, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/delete_market")
    public ResponseEntity<GeneralResponse<?>> deleteMarket(@RequestParam long id, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = marketService.delete(id, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/change_status")
    public ResponseEntity<GeneralResponse<?>> changeStatus(@RequestParam long id, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = marketService.changeStatus(id, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }


}
