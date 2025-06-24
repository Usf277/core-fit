package com.corefit.service.playground;

import com.corefit.dto.request.playground.PlaygroundRateRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.playground.PlaygroundRateResponse;
import com.corefit.entity.auth.User;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.PlaygroundRate;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.PlaygroundRateRepo;
import com.corefit.repository.playground.PlaygroundRepo;
import com.corefit.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlaygroundRateService {
    @Autowired
    private PlaygroundRateRepo playgroundRateRepo;
    @Autowired
    private PlaygroundRepo playgroundRepo;
    @Autowired
    private AuthService authService;

    public GeneralResponse<?> getRatesByPlayground(Long playgroundId, Integer page, Integer size) {
        if (size == null || size <= 0) size = 5;
        if (page == null || page < 1) page = 1;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<PlaygroundRate> rates = playgroundRateRepo.findAllByPlaygroundId(playgroundId, pageable);

        Page<PlaygroundRateResponse> rateResponses = rates.map(this::mapToRateResponse);

        Map<String, Object> data = new HashMap<>();
        data.put("rates", rateResponses.getContent());
        data.put("totalPages", rateResponses.getTotalPages());
        data.put("totalElements", rateResponses.getTotalElements());
        data.put("pageSize", rateResponses.getSize());

        return new GeneralResponse<>("Playground rates retrieved successfully", data);
    }

    public GeneralResponse<?> insert(PlaygroundRateRequest request, HttpServletRequest httpRequest) {
        Playground playground = playgroundRepo.findById(request.getPlaygroundId())
                .orElseThrow(() -> new GeneralException("Playground not found"));

        User user = authService.extractUserFromRequest(httpRequest);
        if (user.getType() == UserType.PROVIDER) {
            throw new GeneralException("User not authorized to rate");
        }

        PlaygroundRate rate = PlaygroundRate.builder()
                .comment(request.getComment())
                .rate(Math.min(5, Math.max(0, request.getRate())))
                .playground(playground)
                .user(user)
                .build();

        rate = playgroundRateRepo.save(rate);

        List<PlaygroundRate> allRates = playgroundRateRepo.findByPlayground(playground);
        double avg = allRates.stream().mapToInt(PlaygroundRate::getRate).average().orElse(0.0);

        playground.setAvgRate((int) Math.round(avg));
        playgroundRepo.save(playground);

        return new GeneralResponse<>("Rate added successfully", mapToRateResponse(rate));
    }

    /// Helper method
    private PlaygroundRateResponse mapToRateResponse(PlaygroundRate rate) {
        return PlaygroundRateResponse.builder()
                .id(rate.getId())
                .comment(rate.getComment())
                .rate(rate.getRate())
                .createdAt(rate.getCreatedAt())
                .username(rate.getUser() != null ? rate.getUser().getUsername() : "Unknown")
                .build();
    }
}
