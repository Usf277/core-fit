package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.ProductRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/add_product", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> insert(@RequestPart("product") ProductRequest productRequest,
                                                     @RequestPart("images") Set<MultipartFile> images) {
        try {
            GeneralResponse<?> response = productService.insert(productRequest, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
