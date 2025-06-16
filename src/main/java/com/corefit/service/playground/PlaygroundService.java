package com.corefit.service.playground;

import com.corefit.dto.request.playground.PlaygroundRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.City;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.User;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.PlaygroundRepo;
import com.corefit.service.helper.CityService;
import com.corefit.service.auth.AuthService;
import com.corefit.utils.DateParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class PlaygroundService {
    @Autowired
    private PlaygroundRepo playgroundRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private CityService cityService;
    @Autowired
    @Lazy
    private PlaygroundFavouriteService playgroundFavouriteService;

    @Transactional
    public GeneralResponse<?> create(PlaygroundRequest playgroundRequest, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        City city = cityService.findById(playgroundRequest.getCityId());

        if (playgroundRequest.getImages() == null || playgroundRequest.getImages().isEmpty()) {
            throw new GeneralException("You must upload at least one image");
        }

        Playground playground = Playground.builder()
                .name(playgroundRequest.getName())
                .description(playgroundRequest.getDescription())
                .city(city)
                .address(playgroundRequest.getAddress())
                .morningShiftStart(DateParser.parseTime(playgroundRequest.getMorningShiftStart()))
                .morningShiftEnd(DateParser.parseTime(playgroundRequest.getMorningShiftEnd()))
                .nightShiftStart(DateParser.parseTime(playgroundRequest.getNightShiftStart()))
                .nightShiftEnd(DateParser.parseTime(playgroundRequest.getNightShiftEnd()))
                .bookingPrice(playgroundRequest.getBookingPrice())
                .teemMembers(playgroundRequest.getTeamMembers())
                .extraNightPrice(playgroundRequest.getExtraNightPrice())
                .hasExtraPrice(playgroundRequest.isHasExtraPrice())
                .images(playgroundRequest.getImages())
                .user(user)
                .isOpened(true)
                .build();

        playgroundRepo.save(playground);
        return new GeneralResponse<>("Playground added successfully", playground);
    }

    @Transactional
    public GeneralResponse<?> update(PlaygroundRequest playgroundRequest, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        Playground playground = findById(playgroundRequest.getId());
        if (!playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You do not have permission to update this playground");
        }

        City city = cityService.findById(playgroundRequest.getCityId());

        if (playgroundRequest.getImages() == null || playgroundRequest.getImages().isEmpty()) {
            throw new GeneralException("You must upload at least one image");
        }

        playground.setName(playgroundRequest.getName());
        playground.setDescription(playgroundRequest.getDescription());
        playground.setAddress(playgroundRequest.getAddress());
        playground.setCity(city);
        playground.setMorningShiftStart(DateParser.parseTime(playgroundRequest.getMorningShiftStart()));
        playground.setMorningShiftEnd(DateParser.parseTime(playgroundRequest.getMorningShiftEnd()));
        playground.setNightShiftStart(DateParser.parseTime(playgroundRequest.getNightShiftStart()));
        playground.setNightShiftEnd(DateParser.parseTime(playgroundRequest.getNightShiftEnd()));
        playground.setBookingPrice(playgroundRequest.getBookingPrice());
        playground.setExtraNightPrice(playgroundRequest.getExtraNightPrice());
        playground.setHasExtraPrice(playgroundRequest.isHasExtraPrice());
        playground.setImages(playgroundRequest.getImages());

        playgroundRepo.save(playground);

        return new GeneralResponse<>("Playground updated successfully", playground);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAll(Integer page, Integer size, String search, Long cityId, Integer avgRate, HttpServletRequest httpRequest) {
        if (size == null || size <= 0) size = 5;
        if (page == null || page < 1) page = 1;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        User user = authService.extractUserFromRequest(httpRequest);

        Map<String, Object> data = new HashMap<>();
        Page<Playground> playgrounds;

        if (user.getType() == UserType.PROVIDER) {
            playgrounds = playgroundRepo.findAllByUserId(user.getId(), pageable);
        } else {
            playgrounds = playgroundRepo.findAllByFilters(search, cityId, avgRate, pageable);
        }

        Set<Long> favIds = playgroundFavouriteService.getFavouritePlaygroundIdsForUser(user.getId());

        playgrounds.forEach(playground -> playground.setFavourite(favIds.contains(playground.getId())));

        data.put("playgrounds", playgrounds.getContent());
        data.put("totalElements", playgrounds.getTotalElements());
        data.put("totalPages", playgrounds.getTotalPages());
        data.put("pageSize", playgrounds.getSize());
        data.put("currentPage", playgrounds.getNumber() + 1);

        return new GeneralResponse<>("Playgrounds retrieved successfully", data);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getById(Long id, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = findById(id);

        if (user.getType() == UserType.PROVIDER && !playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You do not have permission to view this playground");
        }

        boolean isFavorite = playgroundFavouriteService.existsByUserIdAndPlaygroundId(user.getId(), playground.getId());
        playground.setFavourite(isFavorite);

        return new GeneralResponse<>("Playground retrieved successfully", playground);
    }

    /// Helper method
    @Transactional(readOnly = true)
    public Playground findById(Long id) {
        return playgroundRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Playground not found with ID: " + id));
    }
}