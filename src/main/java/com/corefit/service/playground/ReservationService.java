package com.corefit.service.playground;

import com.corefit.dto.request.playground.ReservationRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.User;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
import com.corefit.entity.playground.ReservationSlot;
import com.corefit.repository.playground.ReservationRepo;
import com.corefit.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepo reservationRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private PlaygroundService playgroundService;

    public GeneralResponse<?> bookPlayground(ReservationRequest reservationRequest, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = playgroundService.findById(reservationRequest.getPlaygroundId());

        LocalTime morningStart = playground.getMorningShiftStart();
        LocalTime morningEnd = playground.getMorningShiftEnd();
        LocalTime nightStart = playground.getNightShiftStart();
        LocalTime nightEnd = playground.getNightShiftEnd();

        Set<LocalTime> requestedSlots = new HashSet<>();

        // Step 1: Validate requested slots
        for (ReservationSlot slot : reservationRequest.getSlots()) {
            LocalTime slotTime;
            try {
                slotTime = LocalTime.parse(slot.getTime());
            } catch (Exception e) {
                return new GeneralResponse<>("Invalid time format in slot: " + slot.getTime(), 400);
            }

            boolean inMorning = isWithinShift(slotTime, morningStart, morningEnd);
            boolean inNight = isWithinShift(slotTime, nightStart, nightEnd);

            if (!inMorning && !inNight) {
                return new GeneralResponse<>("Slot " + slotTime + " is outside working hours", 400);
            }

            requestedSlots.add(slotTime);
        }

        // Step 2: Check for conflicts
        List<Reservation> existingReservations = reservationRepo.findByPlaygroundAndDate(playground, String.valueOf(reservationRequest.getDate()));

        Set<LocalTime> reservedSlots = existingReservations.stream()
                .flatMap(r -> r.getSlots().stream())
                .map(ReservationSlot::getTime)
                .map(LocalTime::parse)
                .collect(Collectors.toSet());

        for (LocalTime requestedSlot : requestedSlots) {
            if (reservedSlots.contains(requestedSlot)) {
                return new GeneralResponse<>("Slot already booked: " + requestedSlot, 409);
            }
        }

        // Step 3: Save reservation
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setPlayground(playground);
        reservation.setDate(reservationRequest.getDate());

        // Connect slots to this reservation
        for (ReservationSlot slot : reservationRequest.getSlots()) {
            slot.setReservation(reservation);
        }

        reservation.setSlots(reservationRequest.getSlots());
        reservationRepo.save(reservation);

        return new GeneralResponse<>("Reservation completed successfully", reservation);
    }

    private boolean isWithinShift(LocalTime slot, LocalTime shiftStart, LocalTime shiftEnd) {
        if (shiftEnd.isAfter(shiftStart)) {
            return !slot.isBefore(shiftStart) && !slot.isAfter(shiftEnd);
        } else {
            return !slot.isBefore(shiftStart) || !slot.isAfter(shiftEnd);
        }
    }
}
