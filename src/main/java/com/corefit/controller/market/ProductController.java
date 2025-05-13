package com.corefit.controller.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.market.ProductRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/find_by_id")
    public ResponseEntity<GeneralResponse<?>> getProduct(@RequestParam long id, HttpServletRequest request) {
        try {
            GeneralResponse<?> response = productService.findById(id, request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getAllProduct(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long marketId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) String search,
            HttpServletRequest request) {
        try {
            GeneralResponse<?> response = productService.getAll(page, size, marketId, subCategoryId, search, request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/add_product")
    public ResponseEntity<GeneralResponse<?>> addProduct(@RequestBody ProductRequest productRequest, HttpServletRequest httpRequest) {
        GeneralResponse<?> response = productService.insert(productRequest, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/edit_product")
    public ResponseEntity<GeneralResponse<?>> editProduct(@RequestBody ProductRequest productRequest) {
        GeneralResponse<?> response = productService.update(productRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @DeleteMapping("/delete_product")
    public ResponseEntity<GeneralResponse<?>> deleteProduct(@RequestParam long id) {
        GeneralResponse<?> response = productService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/change_status")
    public ResponseEntity<GeneralResponse<?>> changeStatus(@RequestParam long id) {
        try {
            GeneralResponse<?> response = productService.changeStatus(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
