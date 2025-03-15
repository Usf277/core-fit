package com.corefit.controller.market;

import com.corefit.dto.request.market.FavouriteRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.FavouritesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FavouritesController {

    @Autowired
    private FavouritesService favouritesService;

    @GetMapping("/favourites")
    public ResponseEntity<GeneralResponse<?>> getFavourites(HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = favouritesService.getFavourites(httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/toggle_favourites")
    public ResponseEntity<GeneralResponse<?>> toggleFavourites(@RequestBody FavouriteRequest favouriteRequest, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = favouritesService.toggleFavourite(favouriteRequest, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
