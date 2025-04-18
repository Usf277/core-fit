package com.corefit.controller.helper;

import com.corefit.dto.request.market.CategoryRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping(value = "/categories", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> addCategory(@ModelAttribute CategoryRequest request) {
        try {
            GeneralResponse<?> response = categoryService.saveCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<GeneralResponse<?>> getAllCategories() {
        try {
            GeneralResponse<?> response = categoryService.getAllCategories();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
