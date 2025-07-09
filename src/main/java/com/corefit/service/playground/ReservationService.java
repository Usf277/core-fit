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
import com.corefit.repository.playground.PlaygroundRateRepo;
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
    private PlaygroundRateRepo playgroundRateRepo;
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
        if (!playground.isOpened())
            throw new GeneralException("The playground is Closed");

        User provider = playground.getUser();
        validateRequest(request);

        // Check for conflicts
        Set<LocalTime> requestedTimes = validateAndParseSlots(request.getSlots(), playground);
        List<Reservation> existingReservations = reservationRepo.findActiveByPlaygroundAndDate(playground, request.getDate());

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
        String slotTimes = formatSlots(requestedTimes);

        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            String withdrawPurpose = String.format("Outgoing: payment for reservation at \"%s\" on %s [%s]",
                    playground.getName(), request.getDate(), slotTimes);

            String depositPurpose = String.format("Incoming: received payment for reservation at \"%s\" from %s on %s [%s]",
                    playground.getName(), user.getUsername(), request.getDate(), slotTimes);

            walletService.transfer(user, playground.getUser(), totalCost, withdrawPurpose, depositPurpose);
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .playground(playground)
                .date(request.getDate())
                .price(totalCost)
                .isCancelled(false)
                .isEnded(false)
                .paymentMethod(request.getPaymentMethod())
                .build();

        List<ReservationSlot> slotEntities = requestedTimes.stream()
                .map(time -> ReservationSlot.builder().time(time).reservation(reservation).build())
                .collect(Collectors.toList());

        reservation.setSlots(slotEntities);

        reservationRepo.save(reservation);

        // Send notification
        notificationService.pushNotification(user, "Reservation Confirmed",
                String.format("You successfully booked \"%s\" on %s at %s. Total cost: %.2f EGP.", playground.getName(),
                        request.getDate(), slotTimes, totalCost));

        notificationService.pushNotification(provider, "New Reservation Received",
                String.format("%s has booked \"%s\" on %s at %s. Total price: %.2f EGP.", user.getUsername(),
                        playground.getName(), request.getDate(), slotTimes, totalCost));

        return new GeneralResponse<>("Reservation completed successfully", mapToResponse(reservation));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getReservationDetails(Long reservationId, HttpServletRequest httpRequest) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new GeneralException("Reservation not found"));

        User user = authService.extractUserFromRequest(httpRequest);

        // Access control: GENERAL users can only see their own reservations
        if (user.getType() == UserType.GENERAL) {
            if (!reservation.getUser().getId().equals(user.getId())) {
                throw new GeneralException("You are not authorized to view this reservation");
            }
        } else if (user.getType() == UserType.PROVIDER) {
            if (!reservation.getPlayground().getUser().getId().equals(user.getId())) {
                throw new GeneralException("You are not authorized to view this reservation");
            }
        }

        return new GeneralResponse<>("Reservation details retrieved successfully", mapToResponse(reservation));
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getReservedSlots(Long playgroundId, LocalDate date) {
        Playground playground = playgroundService.findById(playgroundId);

        List<Reservation> reservations = reservationRepo.findActiveByPlaygroundAndDate(playground, date);

        List<String> reservedSlots = reservations.stream()
                .flatMap(r -> r.getSlots().stream())
                .map(slot -> slot.getTime().toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return new GeneralResponse<>("Reserved slots fetched successfully", reservedSlots);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getMyReservations(String status, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        List<Reservation> reservations;
        switch (status.toLowerCase()) {
            case "current" -> reservations = reservationRepo.findCurrentByUser(user);
            case "previous" -> reservations = reservationRepo.findPreviousByUser(user);
            default -> throw new GeneralException("Invalid status. Expected 'current' or 'previous'");
        }

        List<ReservationResponse> responses = reservations.stream().map(this::mapToResponse).toList();

        return new GeneralResponse<>("Reservations retrieved successfully", responses);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getReservations(Long playgroundId, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        if (user.getType() != UserType.PROVIDER)
            throw new GeneralException("User is not authorized to view reservations");

        if (playgroundId == null)
            throw new GeneralException("Playground ID must not be null");

        Playground playground = playgroundService.findById(playgroundId);

        if (!playground.getUser().getId().equals(user.getId()))
            throw new GeneralException("You are not the owner of this playground");

        List<Reservation> reservations = reservationRepo.findByPlayground(playground);

        if (reservations.isEmpty())
            return new GeneralResponse<>("No reservations found for the given playground", List.of());

        List<ReservationResponse> responses = reservations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new GeneralResponse<>("Reservations retrieved successfully", responses);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public GeneralResponse<String> cancelReservation(Long reservationId, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new GeneralException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId()))
            throw new GeneralException("You are not allowed to cancel this reservation.");

        if (reservation.isCancelled())
            throw new GeneralException("Reservation is already cancelled.");

        if (reservation.isEnded())
            throw new GeneralException("Cannot cancel a reservation that has already ended.");


        Playground playground = reservation.getPlayground();
        User provider = playground.getUser();

        if (reservation.getPaymentMethod() == PaymentMethod.WALLET) {
            String slots = formatSlots(reservation.getSlots().stream().map(ReservationSlot::getTime).collect(Collectors.toSet()));

            String withdrawPurpose = String.format("Outgoing: refund to %s for cancelled reservation at \"%s\" on %s [%s]",
                    user.getUsername(), playground.getName(), reservation.getDate(), slots);

            String depositPurpose = String.format("Incoming: refund for cancelled reservation at \"%s\" on %s [%s]",
                    playground.getName(), reservation.getDate(), slots);

            walletService.transfer(provider, user, reservation.getPrice(), depositPurpose, withdrawPurpose);
        }

        reservation.setCancelled(true);
        reservationRepo.save(reservation);

        reservationPasswordRepo.findByReservationId(reservationId).ifPresent(reservationPassword -> {
            reservationPasswordRepo.deleteById(reservationPassword.getId());
            redisTemplate.delete(REDIS_KEY + reservationId);
        });

        // Send notification
        notificationService.pushNotification(user, "❌ Reservation Cancelled",
                buildUserCancelMessage(playground, reservation));

        notificationService.pushNotification(provider, "⚠️ Reservation Cancelled by User",
                buildProviderCancelMessage(playground, reservation, user));

        return new GeneralResponse<>("Reservation cancelled successfully", null);
    }

    @Transactional
    public GeneralResponse<String> generateReservationPassword(Long playgroundId, Long reservationId, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = playgroundService.findById(playgroundId);

        Reservation reservation = reservationRepo.findByIdAndUser(reservationId, user)
                .orElseThrow(() -> new GeneralException("Reservation not found or not owned by user"));

        if (reservation.isCancelled() || reservation.isEnded()) {
            throw new GeneralException("Cannot generate password for a cancelled or ended reservation");
        }

        if (!playground.isPasswordEnabled() || playground.getPassword() == null) {
            throw new GeneralException("Playground does not require a reservation password");
        }

        String redisKey = REDIS_KEY + reservationId;

        // delete old password if exists
        redisTemplate.delete(redisKey);
        reservationPasswordRepo.findByReservationId(reservationId)
                .ifPresent(p -> reservationPasswordRepo.deleteById(p.getId()));

        // fetch all reservation passwords for same playground & same date
        LocalDate today = LocalDate.now();
        List<String> todayHashedPasswords = reservationPasswordRepo
                .findAllByPlaygroundAndDate(playground, today)
                .stream()
                .map(ReservationPassword::getPassword)
                .collect(Collectors.toList());


        String randomPassword = generateRandomPassword(playground.getPassword(), todayHashedPasswords);
        String hashedPassword = passwordEncoder.encode(randomPassword);

        ReservationPassword reservationPassword = ReservationPassword.builder()
                .reservation(reservation)
                .password(hashedPassword)
                .createdAt(LocalDateTime.now())
                .build();

        reservationPasswordRepo.save(reservationPassword);
        redisTemplate.opsForValue().set(redisKey, hashedPassword, 10, TimeUnit.MINUTES);

        notificationService.pushNotification(user, "🔐 Temporary Password Generated",
                "Your temporary password for \"" + playground.getName() + "\" on " + reservation.getDate()
                        + " is: " + randomPassword + ". This password is valid for 10 minutes only.");

        return new GeneralResponse<>("Temporary reservation password generated", randomPassword);
    }

    @Transactional
    public boolean verifyPassword(Long playgroundId, String inputPassword) {
        Playground playground = playgroundService.findById(playgroundId);

        if (playground.isPasswordEnabled() && playground.getPassword() != null && passwordEncoder.matches(inputPassword, playground.getPassword())) {
            return true;
        }

        List<Reservation> reservations = reservationRepo.findActiveReservations(playground, LocalDate.now());
        for (Reservation reservation : reservations) {
            String redisKey = REDIS_KEY + reservation.getId();
            String hashedPassword = redisTemplate.opsForValue().get(redisKey);

            if (hashedPassword != null && passwordEncoder.matches(inputPassword, hashedPassword)) {
                redisTemplate.delete(redisKey);
                reservationPasswordRepo.findByReservationId(reservation.getId())
                        .ifPresent(p -> reservationPasswordRepo.deleteById(p.getId()));

                return true;
            }
        }
        return false;
    }

    /// Helper Methods
    private void validateRequest(ReservationRequest request) {
        if (request.getSlots() == null || request.getSlots().isEmpty()) {
            throw new GeneralException("At least one slot is required");
        }

        LocalDate today = LocalDate.now();
        LocalDate requestedDate = request.getDate();

        if (requestedDate.isBefore(today) || requestedDate.isAfter(today.plusDays(30))) {
            throw new GeneralException("Booking date must be today or within 30 days");
        }

        if (requestedDate.isEqual(today)) {
            LocalTime now = LocalTime.now();
            for (String slot : request.getSlots()) {
                LocalTime slotTime;
                try {
                    slotTime = LocalTime.parse(slot);
                } catch (Exception e) {
                    throw new GeneralException("Invalid time format: " + slot);
                }

                if (slotTime.isBefore(now)) {
                    throw new GeneralException("Cannot book a slot that has already passed: " + slot);
                }
            }
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
        Playground playground = reservation.getPlayground();

        List<String> slotTimes = reservation.getSlots().stream()
                .map(slot -> slot.getTime().toString())
                .collect(Collectors.toList());

        long numberOfRates = playgroundRateRepo.countByPlayground(playground);

        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .date(reservation.getDate())
                .slots(slotTimes)
                .price(reservation.getPrice())
                .cancelled(reservation.isCancelled())
                .ended(reservation.isEnded())
                .createdAt(reservation.getCreatedAt())

                .playgroundId(playground.getId())
                .playgroundName(playground.getName())
                .playgroundImages(playground.getImages())
                .playgroundAddress(playground.getAddress())
                .playgroundAvgRate(playground.getAvgRate())
                .numberPlaygroundRates(numberOfRates)
                .teamMembers(playground.getTeamMembers())
                .build();
    }

    private String formatSlots(Set<LocalTime> times) {
        return times.stream().sorted().map(LocalTime::toString).collect(Collectors.joining(", "));
    }

    private String buildUserCancelMessage(Playground playground, Reservation reservation) {
        String message = String.format("You have cancelled your reservation for \"%s\" on %s.",
                playground.getName(), reservation.getDate());
        if (reservation.getPaymentMethod() == PaymentMethod.WALLET) {
            message += String.format(" An amount of %.2f EGP has been refunded to your wallet.", reservation.getPrice());
        }
        return message;
    }

    private String buildProviderCancelMessage(Playground playground, Reservation reservation, User user) {
        String message = String.format("User %s has cancelled their reservation for \"%s\" on %s.",
                user.getUsername(), playground.getName(), reservation.getDate());
        if (reservation.getPaymentMethod() == PaymentMethod.WALLET) {
            message += String.format(" Refunded: %.2f EGP.", reservation.getPrice());
        }
        return message;
    }

    private String generateRandomPassword(String playgroundPassword, List<String> todayHashedPasswords) {
        Random random = new Random();
        String randomPassword;

        while (true) {
            randomPassword = String.format("%06d", random.nextInt(1000000));

            boolean isDuplicate = false;

            if (playgroundPassword != null && passwordEncoder.matches(randomPassword, playgroundPassword)) {
                isDuplicate = true;
            }

            for (String hashed : todayHashedPasswords) {
                if (passwordEncoder.matches(randomPassword, hashed)) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate) {
                break;
            }
        }
        return randomPassword;
    }
}