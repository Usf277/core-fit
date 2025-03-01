package com.corefit.service;

import com.corefit.config.JwtUtil;
import com.corefit.dto.GeneralResponse;
import com.corefit.dto.MarketRequest;
import com.corefit.entity.Category;
import com.corefit.entity.Market;
import com.corefit.entity.User;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.MarketRepo;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketService {
    @Autowired
    private MarketRepo marketRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FilesService filesService;
    @Autowired
    private CategoryService categoryService;


    public GeneralResponse<?> getMarketById(long id) {
        Market market = marketRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Market not found"));

        Long rateCount = marketRepo.getMarketRateCount(id);
        Double averageRate = marketRepo.getMarketAverageRate(id);


        Map<String, Object> data = new HashMap<>();
        data.put("Market", market);
        data.put("rateCount", rateCount != null ? rateCount : 0);
        data.put("averageRate",  averageRate != null ? averageRate : 0.0);

        return new GeneralResponse<>("Success", data);
    }

    public Page<Market> getAll(Integer page, Integer size, String name, Long categoryId) {
        if (size == null || size <= 0) size = 5;
        if (page == null || page < 1) page = 1;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        return marketRepo.findAllByFilters(name, categoryId, pageable);
    }

    public GeneralResponse<?> insert(MarketRequest request, HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        int userIdInt = Integer.parseInt(userId);

        User user = userRepo.findById(userIdInt)
                .orElseThrow(() -> new GeneralException("User not found"));

        if (!UserType.PROVIDER.equals(user.getType())) {
            throw new GeneralException("User is not a provider");
        }

        Map<String, Object> data = new HashMap<>();
        String imagePath = null;

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                imagePath = filesService.saveImage(request.getImage());
            } catch (IOException e) {
                return new GeneralResponse<>("Failed to upload image: " + e.getMessage());
            }
        }

        Market market = new Market();
        market.setName(request.getName());
        market.setDescription(request.getDescription());

        Category category = categoryService.findById(request.getCategoryId());
        market.setCategory(category);

        market.setLat(request.getLat());
        market.setLng(request.getLng());
        market.setAddress(request.getAddress());
        market.setUser(user);
        market.setImageUrl(imagePath);

        marketRepo.save(market);
        data.put("Market", market);
        return new GeneralResponse<>("Market added successfully", data);
    }

    public GeneralResponse<?> update(MarketRequest request, HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        int userIdInt = Integer.parseInt(userId);

        Market market = marketRepo.findById(request.getId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        if (market.getUser().getId() != userIdInt) {
            throw new GeneralException("User is not owner of this market");
        }

        String imagePath = market.getImageUrl();
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    filesService.deleteImage(imagePath);
                }
                imagePath = filesService.saveImage(request.getImage());
            } catch (IOException e) {
                throw new GeneralException("Failed to upload image: " + e.getMessage());
            }
        }

        Map<String, Object> data = new HashMap<>();
        market.setName(request.getName());
        market.setDescription(request.getDescription());

        Category category = categoryService.findById(request.getCategoryId());
        market.setCategory(category);

        market.setLat(request.getLat());
        market.setLng(request.getLng());
        market.setAddress(request.getAddress());

        marketRepo.save(market);
        data.put("Market", market);
        return new GeneralResponse<>("Market updated successfully", data);
    }

    public GeneralResponse<?> delete(long id, HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        int userIdInt = Integer.parseInt(userId);

        Market market = marketRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Market not found"));

        if (market.getUser().getId() != userIdInt) {
            throw new GeneralException("User is not owner of this market");
        }

        String imagePath = market.getImageUrl();

        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                filesService.deleteImage(imagePath);
            }
        } catch (IOException e) {
            throw new GeneralException("Failed to upload image: " + e.getMessage());
        }

        marketRepo.deleteById(id);
        return new GeneralResponse<>("Market deleted successfully");
    }

    public GeneralResponse<?> changeStatus(long id, HttpServletRequest httpRequest) {
        String userId = authService.extractUserIdFromRequest(httpRequest);
        int userIdInt = Integer.parseInt(userId);

        Market market = marketRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Market not found"));

        if (market.getUser().getId() != userIdInt) {
            throw new GeneralException("User is not owner of this market");
        }
        market.setOpened(!market.isOpened());
        marketRepo.save(market);
        return new GeneralResponse<>("Market status changed successfully");
    }
}