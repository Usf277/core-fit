package com.corefit.controller;

import com.corefit.dto.request.PlaygroundRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.PlaygroundService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/playground")
public class PlaygroundController {

    @Autowired
    private PlaygroundService playgroundService;

    public PlaygroundController(PlaygroundService playgroundService) {
        this.playgroundService = playgroundService;
    }

    @PostMapping(path = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> createPlayground(
            @ModelAttribute PlaygroundRequest playgroundRequest,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = playgroundService.createPlayground(playgroundRequest, images, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
