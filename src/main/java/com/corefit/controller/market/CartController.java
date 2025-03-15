package com.corefit.controller.market;

import com.corefit.dto.request.market.AddCartItemRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public ResponseEntity<GeneralResponse<?>> getCart(HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = cartService.getCart(httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/add_cart_item")
    public ResponseEntity<GeneralResponse<?>> addCart(@RequestBody AddCartItemRequest addCartItemRequest, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = cartService.addItemToCart(httpRequest, addCartItemRequest.getProductId(), addCartItemRequest.getQuantity());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/delete_cart")
    public ResponseEntity<GeneralResponse<?>> deleteCart(HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = cartService.deleteCart(httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

}
