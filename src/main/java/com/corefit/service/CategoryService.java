package com.corefit.service;

import com.corefit.dto.GeneralResponse;
import com.corefit.repository.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class CategoryService {
    @Autowired
    private final CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public GeneralResponse<?> getAllCategories() {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categoryRepo.findAll());
        return new GeneralResponse<>("Success", data);
    }
}
