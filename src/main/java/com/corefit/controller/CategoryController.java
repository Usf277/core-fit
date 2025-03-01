package com.corefit.controller;

import com.corefit.dto.CategoryRequest;
import com.corefit.dto.GeneralResponse;
import com.corefit.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<GeneralResponse<?>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping(value = "/categories", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> saveCategory(@ModelAttribute CategoryRequest request) {
        return ResponseEntity.ok(categoryService.saveCategory(request));
    }
}
