package com.corefit.controller.playground;

import com.corefit.dto.request.playground.PasswordRequest;
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

    @GetMapping
    public ResponseEntity<GeneralResponse<?>> reservationDetails(@RequestParam Long reservationId, HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.reservationDetails(reservationId, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/slots")
    public ResponseEntity<GeneralResponse<?>> getReservedSlots(@RequestParam Long playgroundId, @RequestParam LocalDate date) {
        GeneralResponse<?> response = reservationService.getReservedSlots(playgroundId, date);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<GeneralResponse<?>> getMyReservations(@RequestParam String status, HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.getMyReservations(status, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/playground")
    public ResponseEntity<GeneralResponse<?>> getReservations(@RequestParam Long playgroundId, HttpServletRequest httpRequest) {
        GeneralResponse<?> response = reservationService.getReservations(playgroundId, httpRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<GeneralResponse<String>> cancelReservation(@RequestParam Long id, HttpServletRequest httpRequest) {
        GeneralResponse<String> response = reservationService.cancelReservation(id, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-password")
    public GeneralResponse<String> generateReservationPassword(@RequestParam Long reservationId, @RequestParam Long playgroundId, HttpServletRequest httpRequest) {
        return reservationService.generateReservationPassword(playgroundId, reservationId, httpRequest);
    }

    @PostMapping("/verify-password")
    public GeneralResponse<String> verifyPassword(@RequestBody PasswordRequest request) {
        return reservationService.verifyPassword(request.getPlaygroundId(), request.getPassword());
    }
}
