package com.corefit.service.playground;

import com.corefit.dto.request.playground.ReservationRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.playground.ReservationResponse;
import com.corefit.entity.auth.User;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
import com.corefit.entity.playground.ReservationSlot;
import com.corefit.enums.PaymentMethod;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.ReservationRepo;
import com.corefit.service.auth.AuthService;
import com.corefit.service.auth.WalletService;
import com.corefit.service.helper.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepo reservationRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private PlaygroundService playgroundService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private NotificationService notificationService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public GeneralResponse<?> bookPlayground(ReservationRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = playgroundService.findById(request.getPlaygroundId());
        User provider = playground.getUser();
        validateRequest(request);

        // Check for conflicts
        Set<LocalTime> requestedTimes = validateAndParseSlots(request.getSlots(), playground);
        List<Reservation> existingReservations = reservationRepo.findByPlaygroundAndDate(playground, request.getDate());

        Set<LocalTime> reservedTimes = existingReservations.stream()
                .flatMap(r -> r.getSlots().stream())
                .map(ReservationSlot::getTime)
                .collect(Collectors.toSet());

        List<LocalTime> conflicts = requestedTimes.stream().filter(reservedTimes::contains).toList();

        if (!conflicts.isEmpty()) {
            throw new GeneralException("The following time slots are already reserved: " + conflicts);
        }

        // Calculate cost
        double totalCost = calculateTotalPrice(requestedTimes, playground);
        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.withdraw(httpRequest, totalCost);
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .playground(playground)
                .date(request.getDate())
                .price(totalCost)
                .paymentMethod(request.getPaymentMethod())
                .build();

        List<ReservationSlot> slotEntities = requestedTimes.stream()
                .map(time -> ReservationSlot.builder().time(time).reservation(reservation).build())
                .collect(Collectors.toList());

        reservation.setSlots(slotEntities);

        reservationRepo.save(reservation);


        // Send notification
        notificationService.pushNotification(user, "Reservation Confirmed",
                "Your booking for " + playground.getName() + " on " + playground.getName() + " is confirmed.");

        notificationService.pushNotification(provider, "New Reservation Received",
                "You have received a new reservation from " + user.getUsername() + " for \"" + playground.getName() + "\" at " + reservation.getDate() + ".");

        return new GeneralResponse<>("Reservation completed successfully", mapToResponse(reservation));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getReservedSlots(Long playgroundId, LocalDate date) {
        Playground playground = playgroundService.findById(playgroundId);

        List<Reservation> reservations = reservationRepo.findByPlaygroundAndDate(playground, date);

        List<String> reservedSlots = reservations.stream()
                .flatMap(r -> r.getSlots().stream())
                .map(slot -> slot.getTime().toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return new GeneralResponse<>("Reserved slots fetched successfully", reservedSlots);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getMyReservations(HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        List<Reservation> reservations = reservationRepo.findByUser(user);

        List<ReservationResponse> responses = reservations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new GeneralResponse<>("Reservations retrieved successfully", responses);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getReservations(Long playgroundId, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not authorized to view reservations");
        }

        if (playgroundId == null) {
            throw new GeneralException("Playground ID must not be null");
        }

        Playground playground = playgroundService.findById(playgroundId);

        if (!playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You are not the owner of this playground");
        }

        List<Reservation> reservations = reservationRepo.findByPlayground(playground);

        if (reservations.isEmpty()) {
            return new GeneralResponse<>("No reservations found for the given playground", List.of());
        }

        List<ReservationResponse> responses = reservations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new GeneralResponse<>("Reservations retrieved successfully", responses);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public GeneralResponse<String> cancelReservation(Long reservationId, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Reservation reservation = reservationRepo.findByIdAndUser(reservationId, user)
                .orElseThrow(() -> new GeneralException("Reservation not found or not owned by user"));

        // Refund booking price
        if (reservation.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.deposit(user.getId(), reservation.getPrice());
        }

        reservationRepo.delete(reservation);

        // Send notification
        notificationService.pushNotification(user, "Reservation Cancelled",
                "Your booking for " + reservation.getPlayground().getName() + " on " + reservation.getDate() + " has been cancelled.");

        return new GeneralResponse<>("Reservation cancelled successfully", null);
    }

    /// Helper Methods:
    private void validateRequest(ReservationRequest request) {
        if (request.getSlots() == null || request.getSlots().isEmpty()) {
            throw new GeneralException("At least one slot is required");
        }

        LocalDate today = LocalDate.now();
        if (request.getDate().isBefore(today) || request.getDate().isAfter(today.plusDays(30))) {
            throw new GeneralException("Booking date must be today or within 30 days");
        }
    }

    // Validates and parses input time strings and ensures they fall within the playground’s working shifts.
    private Set<LocalTime> validateAndParseSlots(List<String> slotStrings, Playground playground) {
        if (slotStrings == null || slotStrings.isEmpty()) {
            throw new GeneralException("No time slots provided for reservation.");
        }

        Set<LocalTime> parsedSlots = new HashSet<>();
        for (String timeStr : slotStrings) {
            LocalTime time;
            try {
                time = LocalTime.parse(timeStr);
            } catch (Exception e) {
                throw new GeneralException("Invalid time format: " + timeStr);
            }

            boolean isInMorning = isWithinShift(time, playground.getMorningShiftStart(), playground.getMorningShiftEnd());
            boolean isInNight = isWithinShift(time, playground.getNightShiftStart(), playground.getNightShiftEnd());

            if (!isInMorning && !isInNight) {
                throw new GeneralException("Slot " + time + " is outside of working hours.");
            }

            if (!parsedSlots.add(time)) {
                throw new GeneralException("Duplicate slot provided: " + time);
            }
        }

        return parsedSlots;
    }

    // Checks whether a time is within the bounds of a shift (including overnight shifts).
    private boolean isWithinShift(LocalTime time, LocalTime shiftStart, LocalTime shiftEnd) {
        if (shiftStart == null || shiftEnd == null) return false;

        return shiftEnd.isAfter(shiftStart)
                ? !time.isBefore(shiftStart) && !time.isAfter(shiftEnd)
                : !time.isBefore(shiftStart) || !time.isAfter(shiftEnd); // overnight shift
    }

    // Calculate the total booking price
    private double calculateTotalPrice(Set<LocalTime> requestedTimes, Playground playground) {
        double basePrice = playground.getBookingPrice();
        double extraNightPrice = playground.getExtraNightPrice();
        boolean hasExtraPrice = playground.isHasExtraPrice();

        double totalCost = 0.0;

        for (LocalTime time : requestedTimes) {
            totalCost += basePrice;

            boolean isNightSlot = isWithinShift(time, playground.getNightShiftStart(), playground.getNightShiftEnd());

            if (hasExtraPrice && isNightSlot) {
                totalCost += extraNightPrice;
            }
        }

        return totalCost;
    }

    // Maps a Reservation entity to a DTO.
    private ReservationResponse mapToResponse(Reservation reservation) {
        List<String> slots = reservation.getSlots().stream().map(slot -> slot.getTime().toString())
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .playgroundId(reservation.getPlayground().getId())
                .date(reservation.getDate())
                .slots(slots)
                .price(reservation.getPrice())
                .build();
    }
}