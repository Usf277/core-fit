package com.corefit.controller.playground;

import com.corefit.dto.request.playground.PlaygroundFavouriteRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.playground.PlaygroundFavouriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playground/favourites")
public class PlaygroundFavouriteController {

    @Autowired
    private PlaygroundFavouriteService service;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> toggleFavourite(@RequestBody PlaygroundFavouriteRequest request, HttpServletRequest httpRequest) {
       return ResponseEntity.status(HttpStatus.CREATED).body(service.toggleFavourite(request, httpRequest));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<?>> getFavourites(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(service.getFavourites(httpRequest));
    }
}
