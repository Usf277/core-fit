package com.corefit.service.playground;

import com.corefit.dto.request.playground.PlaygroundRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.helper.City;
import com.corefit.entity.playground.Playground;
import com.corefit.entity.auth.User;
import com.corefit.entity.playground.Reservation;
import com.corefit.enums.PaymentMethod;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.playground.PlaygroundRepo;
import com.corefit.repository.playground.ReservationPasswordRepo;
import com.corefit.repository.playground.ReservationRepo;
import com.corefit.service.auth.AuthService;
import com.corefit.service.auth.WalletService;
import com.corefit.service.helper.CityService;
import com.corefit.service.helper.NotificationService;
import com.corefit.utils.DateParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PlaygroundService {
    private static final String RESERVATION_REDIS_KEY = "reservation:password:";
    @Autowired
    private PlaygroundRepo playgroundRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private CityService cityService;
    @Autowired
    @Lazy
    private PlaygroundFavouriteService playgroundFavouriteService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ReservationRepo reservationRepo;
    @Autowired
    private ReservationPasswordRepo reservationPasswordRepo;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Transactional
    public GeneralResponse<Playground> create(PlaygroundRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        validateProvider(user);

        City city = cityService.findById(request.getCityId());
        validateRequest(request);

        Playground playground = buildPlayground(request, user, city);
        playgroundRepo.save(playground);

        return new GeneralResponse<>("Playground added successfully", playground);
    }

    @Transactional
    public GeneralResponse<Playground> update(PlaygroundRequest request, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        validateProvider(user);

        Playground playground = findById(request.getId());
        validateOwnership(playground, user);

        City city = cityService.findById(request.getCityId());
        validateRequest(request);

        updatePlayground(playground, request, city);
        playgroundRepo.save(playground);

        return new GeneralResponse<>("Playground updated successfully", playground);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<Map<String, Object>> getAll(Integer page, Integer size, String search, Long cityId, Integer avgRate, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);

        Pageable pageable = PageRequest.of(page != null && page >= 1 ? page - 1 : 0, size != null && size > 0 ? size : 5, Sort.by("id").ascending());

        Page<Playground> playgrounds = (user.getType() == UserType.PROVIDER)
                ? playgroundRepo.findAllByUserId(user.getId(), pageable)
                : playgroundRepo.findAllByFilters(search, cityId, avgRate, pageable);

        Set<Long> favIds = playgroundFavouriteService.getFavouritePlaygroundIdsForUser(user.getId());
        playgrounds.forEach(pg -> pg.setFavourite(favIds.contains(pg.getId())));

        Map<String, Object> data = new HashMap<>();
        data.put("playgrounds", playgrounds.getContent());
        data.put("totalElements", playgrounds.getTotalElements());
        data.put("totalPages", playgrounds.getTotalPages());
        data.put("pageSize", playgrounds.getSize());
        data.put("currentPage", playgrounds.getNumber() + 1);

        return new GeneralResponse<>("Playgrounds retrieved successfully", data);
    }

    @Transactional(readOnly = true)
    public GeneralResponse<Playground> getById(Long id, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = findById(id);

        if (user.getType() == UserType.PROVIDER && !playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You do not have permission to access this playground");
        }

        boolean isFavorite = playgroundFavouriteService.existsByUserIdAndPlaygroundId(user.getId(), playground.getId());
        playground.setFavourite(isFavorite);

        return new GeneralResponse<>("Playground retrieved successfully", playground);
    }

    @Transactional
    public GeneralResponse<?> changeStatus(Long id, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = findById(id);

        validateProvider(user);
        validateOwnership(playground, user);

        boolean willBeClosed = playground.isOpened();
        playground.setOpened(!playground.isOpened());
        playgroundRepo.save(playground);

        if (willBeClosed) {
            List<Reservation> activeReservations = reservationRepo.findActiveReservations(playground);

            activeReservations.forEach(reservation -> cancelReservationBySystem(playground, reservation, false));
            reservationRepo.saveAll(activeReservations);

            // Delete all reservation passwords from DB & Redis
            reservationPasswordRepo.deleteAllByPlayground(playground);
            List<String> redisKeys = activeReservations.stream()
                    .map(r -> RESERVATION_REDIS_KEY + r.getId())
                    .toList();
            redisTemplate.delete(redisKeys);
        }

        String statusMessage = playground.isOpened() ? "opened" : "closed";
        notificationService.pushNotification(user, "Playground Status Changed",
                String.format("You have successfully %s your playground \"%s\".", statusMessage, playground.getName()));

        return new GeneralResponse<>("Playground " + statusMessage + " successfully");
    }

    @Transactional
    public GeneralResponse<?> delete(Long id, HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Playground playground = findById(id);

        validateProvider(user);
        validateOwnership(playground, user);

        List<Reservation> reservations = reservationRepo.findByPlayground(playground);

        for (Reservation reservation : reservations) {
            if (!reservation.isCancelled() && !reservation.isEnded())
                cancelReservationBySystem(playground, reservation, true);
        }

        reservationRepo.deleteAll(reservations);
        reservationPasswordRepo.deleteAllByPlayground(playground);

        List<String> redisKeys = reservations.stream()
                .map(r -> RESERVATION_REDIS_KEY + r.getId())
                .toList();
        redisTemplate.delete(redisKeys);

        playgroundRepo.delete(playground);

        notificationService.pushNotification(user, "Playground Removed",
                String.format("You have successfully removed the playground \"%s\". All active reservations have been cancelled and users were notified.", playground.getName()));

        return new GeneralResponse<>("Playground deleted successfully");
    }

    /// Helper Methods
    public Playground findById(Long id) {
        return playgroundRepo.findById(id).orElseThrow(() -> new GeneralException("Playground not found"));
    }

    private void validateProvider(User user) {
        if (user.getType() != UserType.PROVIDER) {
            throw new GeneralException("User is not a provider");
        }
    }

    private void validateOwnership(Playground playground, User user) {
        if (!playground.getUser().getId().equals(user.getId())) {
            throw new GeneralException("You do not have permission to access this playground");
        }
    }

    private void validateRequest(PlaygroundRequest request) {
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new GeneralException("You must upload at least one image");
        }
        if (request.getBookingPrice() < 0 || (request.isHasExtraPrice() && request.getExtraNightPrice() < 0)) {
            throw new GeneralException("Prices must be non-negative");
        }
        if (request.getTeamMembers() == null || request.getTeamMembers() <= 0) {
            throw new GeneralException("Team members must be positive");
        }
        validateShiftTimes(request);

        if (request.isPasswordEnabled()) {
            if (request.getPassword() == null || !Pattern.compile("^\\d{6}$").matcher(request.getPassword().toString()).matches()) {
                throw new GeneralException("Password must be exactly 6 digits");
            }
        }
    }

    private void validateShiftTimes(PlaygroundRequest request) {
        LocalTime morningStart = DateParser.parseTime(request.getMorningShiftStart());
        LocalTime morningEnd = DateParser.parseTime(request.getMorningShiftEnd());
        LocalTime nightStart = DateParser.parseTime(request.getNightShiftStart());
        LocalTime nightEnd = DateParser.parseTime(request.getNightShiftEnd());

        if (morningStart == null || morningEnd == null || nightStart == null || nightEnd == null)
            throw new GeneralException("Shift times must be valid and not null");

        if (!morningStart.isBefore(morningEnd))
            throw new GeneralException("Morning shift start time must be before end time");

        if (nightStart.equals(nightEnd))
            throw new GeneralException("Night shift start and end times must not be the same");

        boolean nightStartsAfterMorningEnd = nightStart.isAfter(morningEnd);
        boolean nightEndsBeforeMorningEnd = nightEnd.isBefore(morningEnd);
        if (!nightStartsAfterMorningEnd && !nightEndsBeforeMorningEnd)
            throw new GeneralException("Night shift must start after morning shift ends");

    }

    private Playground buildPlayground(PlaygroundRequest request, User user, City city) {
        return Playground.builder()
                .name(request.getName())
                .description(request.getDescription())
                .city(city)
                .address(request.getAddress())
                .lat(request.getLat())
                .lng(request.getLng())
                .morningShiftStart(DateParser.parseTime(request.getMorningShiftStart()))
                .morningShiftEnd(DateParser.parseTime(request.getMorningShiftEnd()))
                .nightShiftStart(DateParser.parseTime(request.getNightShiftStart()))
                .nightShiftEnd(DateParser.parseTime(request.getNightShiftEnd()))
                .bookingPrice(request.getBookingPrice())
                .teamMembers(request.getTeamMembers())
                .extraNightPrice(request.getExtraNightPrice())
                .hasExtraPrice(request.isHasExtraPrice())
                .images(request.getImages())
                .user(user)
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword().toString()) : null)
                .passwordEnabled(request.isPasswordEnabled())
                .isOpened(true)
                .build();
    }

    private void updatePlayground(Playground playground, PlaygroundRequest request, City city) {
        playground.setName(request.getName());
        playground.setDescription(request.getDescription());
        playground.setAddress(request.getAddress());
        playground.setLat(request.getLat());
        playground.setLng(request.getLng());
        playground.setCity(city);
        playground.setMorningShiftStart(DateParser.parseTime(request.getMorningShiftStart()));
        playground.setMorningShiftEnd(DateParser.parseTime(request.getMorningShiftEnd()));
        playground.setNightShiftStart(DateParser.parseTime(request.getNightShiftStart()));
        playground.setNightShiftEnd(DateParser.parseTime(request.getNightShiftEnd()));
        playground.setBookingPrice(request.getBookingPrice());
        playground.setExtraNightPrice(request.getExtraNightPrice());
        playground.setHasExtraPrice(request.isHasExtraPrice());
        playground.setImages(request.getImages());
        playground.setTeamMembers(request.getTeamMembers());
        playground.setPassword(request.getPassword() != null ? passwordEncoder.encode(request.getPassword().toString()) : null);
        playground.setPasswordEnabled(request.isPasswordEnabled());
    }

    private void cancelReservationBySystem(Playground playground, Reservation reservation, boolean isDeleteScenario) {
        String action = isDeleteScenario ? "deleted" : "closed";
        String message = String.format(
                "Your reservation at \"%s\" on %s at [%s] was cancelled because the playground was %s.",
                playground.getName(), reservation.getDate(), formatSlots(reservation), action);

        if (reservation.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.deposit(reservation.getUser().getId(), reservation.getPrice());
            message += String.format(" %.2f EGP has been refunded to your wallet.", reservation.getPrice());
        }

        reservation.setCancelled(true);
        notificationService.pushNotification(reservation.getUser(), "❌ Reservation Cancelled", message);
    }

    private String formatSlots(Reservation reservation) {
        return reservation.getSlots().stream()
                .map(slot -> slot.getTime().toString())
                .sorted()
                .collect(Collectors.joining(", "));
    }
}