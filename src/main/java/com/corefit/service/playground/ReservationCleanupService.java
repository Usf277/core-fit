package com.corefit.service.playground;

import com.corefit.entity.playground.Reservation;
import com.corefit.entity.playground.ReservationSlot;
import com.corefit.repository.playground.ReservationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ReservationCleanupService {

    @Autowired
    private ReservationRepo reservationRepo;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void markEndedReservations() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Reservation> reservations = reservationRepo.findByDateAndIsCancelledFalseAndIsEndedFalse(today);

        int updatedCount = 0;

        for (Reservation reservation : reservations) {
            // Get latest slot time
            LocalTime lastSlotTime = reservation.getSlots().stream()
                    .map(ReservationSlot::getTime)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            if (lastSlotTime != null && now.isAfter(lastSlotTime.plusHours(1))) {
                reservation.setEnded(true);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            reservationRepo.saveAll(reservations);
        }
    }
}
