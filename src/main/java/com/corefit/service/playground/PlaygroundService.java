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
import com.corefit.utils.DateParserUtil;
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

    public GeneralResponse<?> createPlayground(PlaygroundRequest playgroundRequest
            , List<MultipartFile> images
            , HttpServletRequest httpRequest) {

        User user = authService.extractUserFromRequest(httpRequest);
        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }

        List<String> imageUrls = uploadImages(images);

        Playground playground = Playground.builder()
                .name(playgroundRequest.getName())
                .description(playgroundRequest.getDescription())
                .lat(playgroundRequest.getLat())
                .lng(playgroundRequest.getLng())
                .address(playgroundRequest.getAddress())
                .teamMembers(playgroundRequest.getTeamMembers())
                .morningShiftStart(DateParserUtil.parseTime(playgroundRequest.getMorningShiftStart()))
                .morningShiftEnd(DateParserUtil.parseTime(playgroundRequest.getMorningShiftEnd()))
                .nightShiftStart(DateParserUtil.parseTime(playgroundRequest.getNightShiftStart()))
                .nightShiftEnd(DateParserUtil.parseTime(playgroundRequest.getNightShiftEnd()))
                .bookingPrice(playgroundRequest.getBookingPrice())
                .extraNightPrice(playgroundRequest.getExtraNightPrice())
                .hasExtraPrice(playgroundRequest.isHasExtraPrice())
                .images(imageUrls)
                .user(user)
                .build();

        playgroundRepo.save(playground);
        return new GeneralResponse<>("Playground added successfully", playground);
    }


    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream().map(image -> {
            try {
                return filesService.saveImage(image);
            } catch (IOException e) {
                throw new GeneralException("Failed to upload image: " + e.getMessage());
            }
        }).collect(Collectors.toList());
    }
}
