package com.corefit.controller.playground;

import com.corefit.dto.request.playground.PlaygroundRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.playground.PlaygroundService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class PlaygroundController {

    @Autowired
    private PlaygroundService playgroundService;

    public PlaygroundController(PlaygroundService playgroundService) {
        this.playgroundService = playgroundService;
    }

    @PostMapping(path = "/create_playground", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> createPlayground(
            @ModelAttribute PlaygroundRequest playgroundRequest,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest httpRequest) {

        GeneralResponse<?> response = playgroundService.create(playgroundRequest, images, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/update_playground", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> updatePlayground(
            @ModelAttribute PlaygroundRequest playgroundRequest,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest httpRequest) {

        GeneralResponse<?> response = playgroundService.update(playgroundRequest, images, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/playgrounds")
    public ResponseEntity<GeneralResponse<?>> getPlaygrounds(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Integer avgRate,
            HttpServletRequest httpRequest) {

        GeneralResponse<?> response = playgroundService.getAll(page, size, search, cityId, avgRate, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/playground")
    public ResponseEntity<GeneralResponse<?>> getPlaygrounds(@RequestParam Long id, HttpServletRequest httpRequest) {
        GeneralResponse<?> response = playgroundService.getById(id, httpRequest);
        return ResponseEntity.ok(response);
    }
}
