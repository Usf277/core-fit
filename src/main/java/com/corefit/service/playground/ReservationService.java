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

        Set<LocalTime> requestedTimes = new HashSet<>();

        // Step 1: Validate times
        for (String timeStr : reservationRequest.getSlots()) {
            LocalTime slotTime;
            try {
                slotTime = LocalTime.parse(timeStr);
            } catch (Exception e) {
                return new GeneralResponse<>("Invalid time format in slot: " + timeStr, 400);
            }

            boolean inMorning = isWithinShift(slotTime, morningStart, morningEnd);
            boolean inNight = isWithinShift(slotTime, nightStart, nightEnd);

            if (!inMorning && !inNight) {
                return new GeneralResponse<>("Slot " + slotTime + " is outside working hours", 400);
            }

            requestedTimes.add(slotTime);
        }

        // Step 2: Check for conflicts
        List<Reservation> existingReservations = reservationRepo.findByPlaygroundAndDate(playground, reservationRequest.getDate());

        Set<LocalTime> reservedTimes = existingReservations.stream()
                .flatMap(r -> r.getSlots().stream())
                .map(s -> LocalTime.parse(s.getTime()))
                .collect(Collectors.toSet());

        for (LocalTime requested : requestedTimes) {
            if (reservedTimes.contains(requested)) {
                return new GeneralResponse<>("Slot already booked: " + requested, 409);
            }
        }

        // Step 3: Build ReservationSlot list
        List<ReservationSlot> slots = reservationRequest.getSlots().stream()
                .map(time -> ReservationSlot.builder().time(time).build())
                .collect(Collectors.toList());

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setPlayground(playground);
        reservation.setDate(reservationRequest.getDate());
        reservation.setSlots(slots);

        // Back-reference
        slots.forEach(slot -> slot.setReservation(reservation));

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
