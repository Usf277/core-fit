package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.ProductRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.ProductService;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getAllProduct(@RequestParam int page, @RequestParam int size
            , @RequestParam(required = false) Long marketId
            , @RequestParam(required = false) Long subCategoryId
            , @RequestParam(required = false) String name
            , HttpServletRequest request) {
        return ResponseEntity.ok(productService.getAll(page, size, marketId, subCategoryId, name, request));
    }

    @PostMapping(value = "/add_product", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> addProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("offer") int offer,
            @RequestParam("marketId") Long marketId,
            @RequestParam("subCategoryId") Long subCategoryId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            ProductRequest productRequest = new ProductRequest();
            productRequest.setName(name);
            productRequest.setDescription(description);
            productRequest.setPrice(price);
            productRequest.setOffer(offer);
            productRequest.setMarketId(marketId);
            productRequest.setSubCategoryId(subCategoryId);

            GeneralResponse<?> response = productService.insert(productRequest, images);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/edit_product", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> editProduct(
            @RequestParam("id") long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("offer") int offer,
            @RequestParam("subCategoryId") Long subCategoryId,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        try {
            ProductRequest productRequest = new ProductRequest();
            productRequest.setId(id);
            productRequest.setName(name);
            productRequest.setDescription(description);
            productRequest.setPrice(price);
            productRequest.setOffer(offer);
            productRequest.setSubCategoryId(subCategoryId);

            GeneralResponse<?> response = productService.update(productRequest, images);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/delete_product")
    public ResponseEntity<GeneralResponse<?>> deleteProduct(@RequestParam long id) {
        try {
            GeneralResponse<?> response = productService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PutMapping("/change_status")
    public ResponseEntity<GeneralResponse<?>> changeStatus(@RequestParam long id) {
        try {
            GeneralResponse<?> response = productService.changeStatus(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

}
