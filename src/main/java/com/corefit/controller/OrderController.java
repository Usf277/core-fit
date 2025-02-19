package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.OrderRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create_order")
    public ResponseEntity<GeneralResponse<?>> createOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = orderService.createOrder(orderRequest, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
