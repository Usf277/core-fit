package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    @Autowired
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<GeneralResponse<?>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
