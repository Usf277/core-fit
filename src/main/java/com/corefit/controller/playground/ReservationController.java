package com.corefit.controller.playground;

import com.corefit.dto.request.playground.ReservationRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.playground.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping("/book")
    public ResponseEntity<GeneralResponse<?>> bookPlayground(
            @RequestBody ReservationRequest reservationRequest,
            HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.bookPlayground(reservationRequest, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/slots")
    public ResponseEntity<GeneralResponse<?>> getReservedSlots(@RequestParam Long playgroundId, @RequestParam LocalDate date) {
        GeneralResponse<?> response = reservationService.getReservedSlots(playgroundId, date);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<GeneralResponse<?>> getMyReservations(HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.getMyReservations(httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/playground")
    public ResponseEntity<GeneralResponse<?>> getReservations(@RequestParam Long playgroundId, HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.getReservations(playgroundId, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<GeneralResponse<String>> cancelReservation(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        GeneralResponse<String> response = reservationService.cancelReservation(id, httpRequest);
        return ResponseEntity.ok(response);
    }
}
