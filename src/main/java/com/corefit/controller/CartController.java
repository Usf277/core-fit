package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {


    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public ResponseEntity<GeneralResponse<?>> getCart(HttpServletRequest httpRequest) {
      try {
        GeneralResponse<?> response = cartService.getCart(httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (
    GeneralException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GeneralResponse<>(e.getMessage()));
    }
    }

}
