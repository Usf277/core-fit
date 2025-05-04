package com.corefit.service.playground;

import com.corefit.dto.request.playground.PlaygroundRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.User;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.PlaygroundRepo;
import com.corefit.service.helper.FilesService;
import com.corefit.service.auth.AuthService;
import com.corefit.utils.DateParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class PlaygroundService {
    @Autowired
    private PlaygroundRepo playgroundRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private FilesService filesService;

    public GeneralResponse<?> create(PlaygroundRequest playgroundRequest, List<MultipartFile> images, HttpServletRequest httpRequest) {

        User user = authService.extractUserFromRequest(httpRequest);
        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        List<String> imageUrls = filesService.uploadImages(images);

        Playground playground = Playground.builder()
                .name(playgroundRequest.getName())
                .description(playgroundRequest.getDescription())
                .lat(playgroundRequest.getLat())
                .lng(playgroundRequest.getLng())
                .address(playgroundRequest.getAddress())
                .teamMembers(playgroundRequest.getTeamMembers())
                .morningShiftStart(DateParser.parseTime(playgroundRequest.getMorningShiftStart()))
                .morningShiftEnd(DateParser.parseTime(playgroundRequest.getMorningShiftEnd()))
                .nightShiftStart(DateParser.parseTime(playgroundRequest.getNightShiftStart()))
                .nightShiftEnd(DateParser.parseTime(playgroundRequest.getNightShiftEnd()))
                .bookingPrice(playgroundRequest.getBookingPrice())
                .extraNightPrice(playgroundRequest.getExtraNightPrice())
                .hasExtraPrice(playgroundRequest.isHasExtraPrice())
                .images(imageUrls)
                .user(user)
                .isOpened(true)
                .build();

        playgroundRepo.save(playground);
        return new GeneralResponse<>("Playground added successfully", playground);
    }

    public GeneralResponse<?> update(PlaygroundRequest playgroundRequest, List<MultipartFile> images, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        Playground playground = findById(playgroundRequest.getId());
        if (!playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You do not have permission to update this playground");
        }

        filesService.deleteImages(playground.getImages());
        List<String> imageUrls = (images != null && !images.isEmpty()) ? filesService.uploadImages(images) : playground.getImages();

        playground.setName(playgroundRequest.getName());
        playground.setDescription(playgroundRequest.getDescription());
        playground.setLat(playgroundRequest.getLat());
        playground.setLng(playgroundRequest.getLng());
        playground.setAddress(playgroundRequest.getAddress());
        playground.setTeamMembers(playgroundRequest.getTeamMembers());
        playground.setMorningShiftStart(DateParser.parseTime(playgroundRequest.getMorningShiftStart()));
        playground.setMorningShiftEnd(DateParser.parseTime(playgroundRequest.getMorningShiftEnd()));
        playground.setNightShiftStart(DateParser.parseTime(playgroundRequest.getNightShiftStart()));
        playground.setNightShiftEnd(DateParser.parseTime(playgroundRequest.getNightShiftEnd()));
        playground.setBookingPrice(playgroundRequest.getBookingPrice());
        playground.setExtraNightPrice(playgroundRequest.getExtraNightPrice());
        playground.setHasExtraPrice(playgroundRequest.isHasExtraPrice());
        playground.setImages(imageUrls);

        playgroundRepo.save(playground);

        return new GeneralResponse<>("Playground updated successfully", playground);
    }

    public GeneralResponse<?> getAll(Integer page, Integer size, String search, HttpServletRequest httpRequest) {
        if (size == null || size <= 0) size = 5;
        if (page == null || page < 1) page = 1;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        User user = authService.extractUserFromRequest(httpRequest);

        Map<String, Object> data = new HashMap<>();

        if (user.getType() == UserType.PROVIDER) {
            Page<Playground> playgrounds = playgroundRepo.findAllByUserId(user.getId(), pageable);
            data.put("playgrounds", playgrounds.getContent());
            data.put("totalElements", playgrounds.getTotalElements());
            data.put("totalPages", playgrounds.getTotalPages());
            data.put("pageSize", playgrounds.getSize());
        } else {
            Page<Playground> playgrounds = playgroundRepo.findAllByFilters(search, pageable);
            data.put("playgrounds", playgrounds.getContent());
            data.put("totalElements", playgrounds.getTotalElements());
            data.put("totalPages", playgrounds.getTotalPages());
            data.put("pageSize", playgrounds.getSize());
        }

        return new GeneralResponse<>("Playgrounds retrieved successfully", data);
    }


    /// Helper method
    private Playground findById(Long id) {
        return playgroundRepo.findById(id).orElseThrow(() -> new GeneralException("Playground not found"));
    }
}
