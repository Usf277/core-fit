package com.corefit.repository.playground;

import com.corefit.entity.playground.ReservationPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationPasswordRepo extends JpaRepository<ReservationPassword, Long> {
    @Query("SELECT rp FROM ReservationPassword rp WHERE rp.reservation.id = :reservationId")
    Optional<ReservationPassword> findByReservationId(Long reservationId);
}