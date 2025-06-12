package com.corefit.controller.playground;

import com.corefit.dto.request.playground.ReservationRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.playground.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> bookPlayground(
            @RequestBody ReservationRequest reservationRequest,
            HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.bookPlayground(reservationRequest, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<?>> getReservations(HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.getReservations(httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<String>> cancelReservation(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        GeneralResponse<String> response = reservationService.cancelReservation(id, httpRequest);
        return ResponseEntity.ok(response);
    }
}
