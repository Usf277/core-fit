package com.corefit.service;

import com.corefit.dto.request.CategoryRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.Category;
import com.corefit.repository.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private FilesService filesService;

    public Category findById(long id) {
        return categoryRepo.findById(id).get();
    }

    public GeneralResponse<?> getAllCategories() {
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categoryRepo.findAll());
        return new GeneralResponse<>("Success", data);
    }

    public GeneralResponse<?> saveCategory(CategoryRequest request) {
        Map<String, Object> data = new HashMap<>();

        String imagePath = null;

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                imagePath = filesService.saveImage(request.getImage());
            } catch (IOException e) {
                return new GeneralResponse<>("Failed to upload image: " + e.getMessage(), null);
            }
        }

        Category category = new Category();

        category.setName(request.getName());
        category.setImageUrl(imagePath);

        categoryRepo.save(category);
        data.put("category", category);
        return new GeneralResponse<>("Category saved successfully", data);
    }
}
