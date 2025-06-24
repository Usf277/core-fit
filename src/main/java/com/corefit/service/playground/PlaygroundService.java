package com.corefit.service.playground;

import com.corefit.dto.request.playground.PlaygroundRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.City;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.User;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.PlaygroundRepo;
import com.corefit.service.auth.AuthService;
import com.corefit.service.helper.CityService;
import com.corefit.utils.DateParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public GeneralResponse<Playground> create(PlaygroundRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        validateProvider(user);

        City city = cityService.findById(request.getCityId());
        validateRequest(request);

        Playground playground = buildPlayground(request, user, city);
        playgroundRepo.save(playground);

        return new GeneralResponse<>("Playground added successfully", playground);
    }

    @Transactional
    public GeneralResponse<Playground> update(PlaygroundRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        validateProvider(user);

        Playground playground = findById(request.getId());
        validateOwnership(playground, user);

        City city = cityService.findById(request.getCityId());
        validateRequest(request);

        updatePlayground(playground, request, city);
        playgroundRepo.save(playground);

        return new GeneralResponse<>("Playground updated successfully", playground);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<Map<String, Object>> getAll(Integer page, Integer size, String search, Long cityId, Integer avgRate, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        Pageable pageable = PageRequest.of(page != null && page >= 1 ? page - 1 : 0, size != null && size > 0 ? size : 5,
                Sort.by("id").ascending());

        Page<Playground> playgrounds;
        if (user.getType() == UserType.PROVIDER) {
            playgrounds = playgroundRepo.findAllByUserId(user.getId(), pageable);
        } else {
            playgrounds = playgroundRepo.findAllByFilters(search, cityId, avgRate, pageable);
        }

        Set<Long> favIds = playgroundFavouriteService.getFavouritePlaygroundIdsForUser(user.getId());
        playgrounds.forEach(playground -> playground.setFavourite(favIds.contains(playground.getId())));

        Map<String, Object> data = new HashMap<>();
        data.put("playgrounds", playgrounds.getContent());
        data.put("totalElements", playgrounds.getTotalElements());
        data.put("totalPages", playgrounds.getTotalPages());
        data.put("pageSize", playgrounds.getSize());
        data.put("currentPage", playgrounds.getNumber() + 1);

        return new GeneralResponse<>("Playgrounds retrieved successfully", data);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<Playground> getById(Long id, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = findById(id);

        if (user.getType() == UserType.PROVIDER && !playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You do not have permission to access this playground");
        }

        boolean isFavorite = playgroundFavouriteService.existsByUserIdAndPlaygroundId(user.getId(), playground.getId());
        playground.setFavourite(isFavorite);

        return new GeneralResponse<>("Playground retrieved successfully", playground);
    }

    /// Helper Methods
    public Playground findById(Long id) {
        return playgroundRepo.findById(id)
                .orElseThrow(() -> new GeneralException("Playground not found"));
    }

    private void validateProvider(User user) {
        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }
    }

    private void validateOwnership(Playground playground, User user) {
        if (!playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You do not have permission to access this playground");
        }
    }

    private void validateRequest(PlaygroundRequest request) {
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new GeneralException("You must upload at least one image");
        }

        if (request.getBookingPrice() < 0 || (request.isHasExtraPrice() && request.getExtraNightPrice() < 0)) {
            throw new GeneralException("Prices must be non-negative");
        }

        if (request.getTeamMembers() <= 0) {
            throw new GeneralException("Team members must be positive");
        }

        validateShiftTimes(request);
        validatePassword(request);
    }

    private void validateShiftTimes(PlaygroundRequest request) {
        LocalTime morningStart = DateParser.parseTime(request.getMorningShiftStart());
        LocalTime morningEnd = DateParser.parseTime(request.getMorningShiftEnd());
        LocalTime nightStart = DateParser.parseTime(request.getNightShiftStart());
        LocalTime nightEnd = DateParser.parseTime(request.getNightShiftEnd());

        if (morningStart == null || morningEnd == null || nightStart == null || nightEnd == null) {
            throw new GeneralException("Shift times must be valid and start before end");
        }

        if (morningStart.isAfter(morningEnd) || nightStart.isAfter(nightEnd) || morningEnd.isAfter(nightStart)) {
            throw new GeneralException("Shift times must be valid and start before end");
        }
    }

    private void validatePassword(PlaygroundRequest request) {
        if (request.getPassword() != null && !Pattern.compile("^\\d{6}$").matcher(request.getPassword().toString()).matches()) {
            throw new GeneralException("Password must be exactly 6 digits");
        }
    }

    private Playground buildPlayground(PlaygroundRequest request, User user, City city) {
        return Playground.builder()
                .name(request.getName())
                .description(request.getDescription())
                .city(city)
                .address(request.getAddress())
                .morningShiftStart(DateParser.parseTime(request.getMorningShiftStart()))
                .morningShiftEnd(DateParser.parseTime(request.getMorningShiftEnd()))
                .nightShiftStart(DateParser.parseTime(request.getNightShiftStart()))
                .nightShiftEnd(DateParser.parseTime(request.getNightShiftEnd()))
                .bookingPrice(request.getBookingPrice())
                .teamMembers(request.getTeamMembers())
                .extraNightPrice(request.getExtraNightPrice())
                .hasExtraPrice(request.isHasExtraPrice())
                .images(request.getImages())
                .user(user)
                .password(request.getPassword() != null ?
                        passwordEncoder.encode(request.getPassword().toString()) : null)
                .isOpened(true)
                .build();
    }

    private void updatePlayground(Playground playground, PlaygroundRequest request, City city) {
        playground.setName(request.getName());
        playground.setDescription(request.getDescription());
        playground.setAddress(request.getAddress());
        playground.setCity(city);
        playground.setMorningShiftStart(DateParser.parseTime(request.getMorningShiftStart()));
        playground.setMorningShiftEnd(DateParser.parseTime(request.getMorningShiftEnd()));
        playground.setNightShiftStart(DateParser.parseTime(request.getNightShiftStart()));
        playground.setNightShiftEnd(DateParser.parseTime(request.getNightShiftEnd()));
        playground.setBookingPrice(request.getBookingPrice());
        playground.setExtraNightPrice(request.getExtraNightPrice());
        playground.setHasExtraPrice(request.isHasExtraPrice());
        playground.setImages(request.getImages());
        playground.setTeamMembers(request.getTeamMembers());
        playground.setPassword(request.getPassword() != null ?
                passwordEncoder.encode(request.getPassword().toString()) : null);
    }
}