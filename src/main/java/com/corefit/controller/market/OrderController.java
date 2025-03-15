package com.corefit.controller.market;

import com.corefit.dto.request.market.ChangeStatusRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.market.OrderRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create_order")
    public ResponseEntity<GeneralResponse<?>> createOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = orderService.createOrder(orderRequest, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/order")
    public ResponseEntity<GeneralResponse<?>> getOrder(@RequestParam long orderId, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = orderService.getOrder(orderId, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<GeneralResponse<?>> getOrders(@RequestParam(required = false) String status
            , @RequestParam(required = false) Long marketId
            , HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = orderService.getOrders(status, marketId, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/cancel_order")
    public ResponseEntity<GeneralResponse<?>> cancelOrder(@RequestParam long orderId, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = orderService.cancelOrder(orderId, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/change_status")
    public ResponseEntity<GeneralResponse<?>> changeStatus(@RequestBody ChangeStatusRequest request, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = orderService.changeStatus(request, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }


}
