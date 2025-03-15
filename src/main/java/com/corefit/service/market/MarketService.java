package com.corefit.service.market;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.market.MarketRequest;
import com.corefit.entity.market.Category;
import com.corefit.entity.market.Market;
import com.corefit.entity.User;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.market.MarketRepo;
import com.corefit.service.FilesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MarketService {
    @Autowired
    private MarketRepo marketRepo;
    @Autowired
    private AuthService authService;
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
        data.put("averageRate", averageRate != null ? averageRate : 0.0);

        return new GeneralResponse<>("Success", data);
    }

    public GeneralResponse<?> getAll(Integer page, Integer size, String name, Long categoryId) {
        if (size == null || size <= 0) size = 5;
        if (page == null || page < 1) page = 1;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Market> markets = marketRepo.findAllByFilters(name, categoryId, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("markets", markets.getContent());
        data.put("totalPages", markets.getTotalPages());
        data.put("totalElements", markets.getTotalElements());
        data.put("pageSize", markets.getSize());
        return new GeneralResponse<>("Market retrieved successfully", data);

    }

    public GeneralResponse<?> insert(MarketRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        if (user.getType() != UserType.PROVIDER) {
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
        long userId = authService.extractUserIdFromRequest(httpRequest);

        Market market = marketRepo.findById(request.getId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        if (market.getUser().getId() != userId) {
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
        long userId = authService.extractUserIdFromRequest(httpRequest);

        Market market = marketRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Market not found"));

        if (market.getUser().getId() != userId) {
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
        long userId = authService.extractUserIdFromRequest(httpRequest);

        Market market = marketRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Market not found"));

        if (market.getUser().getId() != userId) {
            throw new GeneralException("User is not owner of this market");
        }
        market.setOpened(!market.isOpened());
        marketRepo.save(market);
        return new GeneralResponse<>("Market status changed successfully");
    }
}