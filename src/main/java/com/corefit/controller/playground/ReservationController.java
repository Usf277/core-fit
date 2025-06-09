package com.corefit.controller.playground;

import com.corefit.dto.request.playground.ReservationRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.playground.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping(path = "/book_playground")
    public ResponseEntity<GeneralResponse<?>> bookPlayground(
            @RequestBody ReservationRequest reservationRequest,
            HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.bookPlayground(reservationRequest, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
