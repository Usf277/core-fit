package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.SubCategoryRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class subCategoryController {
    @Autowired
    private  SubCategoryService subCategoryService;

    @GetMapping("/sub_categories")
    public ResponseEntity<?> getSubCategoriesByMarketId(@RequestParam long marketId) {
        return ResponseEntity.ok(subCategoryService.getSubCategoriesByMarketId(marketId));
    }

    @PostMapping(value = "/add_sub_category")
    public ResponseEntity<GeneralResponse<?>> addSubCategory(@RequestBody SubCategoryRequest request) {
        try {
            GeneralResponse<?> response = subCategoryService.insert(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/edit_sub_category")
    public ResponseEntity<GeneralResponse<?>> editSubCategory(@RequestBody SubCategoryRequest request) {
        try {
            GeneralResponse<?> response = subCategoryService.update(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/delete_sub_category")
    public ResponseEntity<GeneralResponse<?>> deleteSubCategory(@RequestParam long id) {
        try {
            GeneralResponse<?> response = subCategoryService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

}
