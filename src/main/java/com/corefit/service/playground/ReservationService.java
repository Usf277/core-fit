package com.corefit.service.playground;

import com.corefit.dto.request.playground.ReservationRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.playground.ReservationResponse;
import com.corefit.entity.auth.User;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
import com.corefit.entity.playground.ReservationPassword;
import com.corefit.entity.playground.ReservationSlot;
import com.corefit.enums.PaymentMethod;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.ReservationPasswordRepo;
import com.corefit.repository.playground.ReservationRepo;
import com.corefit.service.auth.AuthService;
import com.corefit.service.auth.WalletService;
import com.corefit.service.helper.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepo reservationRepo;
    @Autowired
    private ReservationPasswordRepo reservationPasswordRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private PlaygroundService playgroundService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private static final String REDIS_KEY = "reservation:password:";

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public GeneralResponse<?> bookPlayground(ReservationRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = playgroundService.findById(request.getPlaygroundId());
        if (!playground.isOpened()) {
            throw new GeneralException("The playground is Closed");
        }
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
                "Your booking for " + playground.getName() + " on " + request.getDate() + " is confirmed.");

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

        // Delete associated reservation password
        reservationPasswordRepo.findByReservationId(reservationId).ifPresent(reservationPassword -> {
            reservationPasswordRepo.deleteById(reservationPassword.getId());
            redisTemplate.delete(REDIS_KEY + reservationId);
        });

        reservationRepo.delete(reservation);

        // Send notification
        notificationService.pushNotification(user, "Reservation Cancelled",
                "Your booking for " + reservation.getPlayground().getName() + " on " + reservation.getDate() + " has been cancelled.");

        return new GeneralResponse<>("Reservation cancelled successfully", null);
    }

    @Transactional
    public GeneralResponse<String> generateReservationPassword(Long playgroundId, Long reservationId, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = playgroundService.findById(playgroundId);

        Reservation reservation = reservationRepo.findByIdAndUser(reservationId, user)
                .orElseThrow(() -> new GeneralException("Reservation not found or not owned by user"));

        if (!playground.isPasswordEnabled() || playground.getPassword() == null) {
            throw new GeneralException("Playground does not require a reservation password");
        }

        String randomPassword = generateRandomPassword(playground.getPassword());
        String hashedPassword = passwordEncoder.encode(randomPassword);

        ReservationPassword reservationPassword = ReservationPassword.builder()
                .reservation(reservation)
                .password(hashedPassword)
                .createdAt(LocalDateTime.now())
                .build();
        reservationPasswordRepo.save(reservationPassword);

        // Cache in Redis with 10-minute TTL
        redisTemplate.opsForValue().set(REDIS_KEY + reservationId, hashedPassword, 10, TimeUnit.MINUTES);

        // Schedule deletion after 10 minutes
        schedulePasswordDeletion(reservationId, reservationPassword.getId());

        // Send notification with password
        notificationService.pushNotification(user, "Temporary Password Generated",
                "Your temporary password for reservation at " + playground.getName() + " is: " + randomPassword);

        return new GeneralResponse<>("Temporary reservation password generated", randomPassword);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<String> verifyPassword(Long playgroundId, String password) {
        Playground playground = playgroundService.findById(playgroundId);
        LocalDateTime now = LocalDateTime.now();

        // Check owner password
        if (playground.isPasswordEnabled() && playground.getPassword() != null && passwordEncoder.matches(password, playground.getPassword())) {
            return new GeneralResponse<>("Access granted", "Owner password verified");
        }

        // Check reservation passwords
        List<Reservation> reservations = reservationRepo.findByPlaygroundAndDate(playground, now.toLocalDate());
        for (Reservation reservation : reservations) {
            ReservationPassword reservationPassword = reservationPasswordRepo.findByReservationId(reservation.getId()).orElse(null);
            if (reservationPassword != null && passwordEncoder.matches(password, reservationPassword.getPassword())) {
                if (now.isBefore(reservationPassword.getCreatedAt().plusMinutes(10))) {
                    return new GeneralResponse<>("Access granted", "Reservation password verified");
                }
            }
        }
        throw new GeneralException("Invalid password or expired");
    }

    /// Helper Methods
    private void validateRequest(ReservationRequest request) {
        if (request.getSlots() == null || request.getSlots().isEmpty()) {
            throw new GeneralException("At least one slot is required");
        }

        LocalDate today = LocalDate.now();
        if (request.getDate().isBefore(today) || request.getDate().isAfter(today.plusDays(30))) {
            throw new GeneralException("Booking date must be today or within 30 days");
        }
    }

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

    private boolean isWithinShift(LocalTime time, LocalTime shiftStart, LocalTime shiftEnd) {
        if (shiftStart == null || shiftEnd == null) return false;

        return shiftEnd.isAfter(shiftStart)
                ? !time.isBefore(shiftStart) && !time.isAfter(shiftEnd)
                : !time.isBefore(shiftStart) || !time.isAfter(shiftEnd); // overnight shift
    }

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

    private String generateRandomPassword(String playgroundPassword) {
        Random random = new Random();
        String randomPassword;
        do {
            randomPassword = String.format("%06d", random.nextInt(1000000));
        } while (playgroundPassword != null && passwordEncoder.matches(randomPassword, playgroundPassword));
        return randomPassword;
    }

    private void schedulePasswordDeletion(Long reservationId, Long reservationPasswordId) {
        new Thread(() -> {
            try {
                Thread.sleep(10 * 60 * 1000); // 10 minutes
                reservationPasswordRepo.deleteById(reservationPasswordId);
                redisTemplate.delete(REDIS_KEY + reservationId);
            } catch (Exception e) {
                throw new GeneralException("Password could not be deleted.");
            }
        }).start();
    }
}