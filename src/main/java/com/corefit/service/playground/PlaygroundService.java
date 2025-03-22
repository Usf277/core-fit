package com.corefit.service.playground;

import com.corefit.dto.request.playground.PlaygroundRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.User;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.PlaygroundRepo;
import com.corefit.service.FilesService;
import com.corefit.service.market.AuthService;
import com.corefit.utils.DateParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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


    public GeneralResponse<?> getAll(HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        if (user == null || user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        List<Playground> playgrounds = playgroundRepo.findAllByUser_Id(user.getId());

        return new GeneralResponse<>("playgrounds retrieved successfully", playgrounds);
    }

    /// Helper method
    private Playground findById(Long id) {
        return playgroundRepo.findById(id).orElseThrow(() -> new GeneralException("Playground not found"));
    }
}
