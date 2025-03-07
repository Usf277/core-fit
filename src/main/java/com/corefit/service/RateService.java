package com.corefit.service;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.RateRequest;
import com.corefit.entity.*;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.MarketRepo;
import com.corefit.repository.RateRepo;
import com.corefit.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RateService {

    @Autowired
    private RateRepo rateRepo;
    @Autowired
    private MarketRepo marketRepo;
    @Autowired
    private UserRepo userRepo;

    public GeneralResponse<?> findById(long id) {
        Rate rate = rateRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Rate not found"));
        return new GeneralResponse<>("Success", rate);
    }

    public GeneralResponse<?> getRatesByMarket(Long marketId, Integer page, Integer size) {
        if (size == null || size <= 0) size = 5;
        if (page == null || page < 1) page = 1;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Rate> rates = rateRepo.getRatesByMarketId(marketId, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("rates", rates.getContent());
        data.put("totalPages", rates.getTotalPages());
        data.put("totalElements", rates.getTotalElements());
        return new GeneralResponse<>("Market rates retrieved successfully", data);
    }

    public GeneralResponse<?> insert(RateRequest request) {
        Market market = marketRepo.findById(request.getMarketId())
                .orElseThrow(() -> new GeneralException("Market not found"));

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new GeneralException("User not found"));

        Rate rate = Rate.builder()
                .comment(request.getComment())
                .rate(Math.min(5, Math.max(0, request.getRate()))) // rate is between 0 , 5
                .market(market)
                .user(user)
                .build();

        rate = rateRepo.save(rate);

        Map<String, Object> data = new HashMap<>();
        data.put("rate", rate);

        return new GeneralResponse<>("Rate added successfully", data);
    }
}