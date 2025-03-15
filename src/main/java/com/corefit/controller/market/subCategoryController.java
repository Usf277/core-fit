package com.corefit.controller.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.market.SubCategoryRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.SubCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class subCategoryController {
    @Autowired
    private SubCategoryService subCategoryService;

    @GetMapping("/sub_categories")
    public ResponseEntity<?> getSubCategoriesByMarketId(@RequestParam long marketId) {
        try {
            GeneralResponse<?> response = subCategoryService.getSubCategoriesByMarketId(marketId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/add_sub_category")
    public ResponseEntity<GeneralResponse<?>> addSubCategory(@RequestBody SubCategoryRequest request) {
        try {
            GeneralResponse<?> response = subCategoryService.insert(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/edit_sub_category")
    public ResponseEntity<GeneralResponse<?>> editSubCategory(@RequestBody SubCategoryRequest request) {
        try {
            GeneralResponse<?> response = subCategoryService.update(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping("/delete_sub_category")
    public ResponseEntity<GeneralResponse<?>> deleteSubCategory(@RequestParam long id) {
        try {
            GeneralResponse<?> response = subCategoryService.delete(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
