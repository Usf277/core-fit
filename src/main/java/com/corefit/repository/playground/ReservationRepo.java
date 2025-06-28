package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
import com.corefit.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByPlaygroundAndDate(Playground playground, LocalDate date);

    List<Reservation> findByUser(User user);

    Optional<Reservation> findByIdAndUser(Long id, User user);

    List<Reservation> findByPlayground(Playground playground);

    List<Reservation> findByDateAndIsCancelledFalseAndIsEndedFalse(LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.isCancelled = false AND r.isEnded = false")
    List<Reservation> findCurrentByUser(User user);

    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND (r.isCancelled = true OR r.isEnded = true)")
    List<Reservation> findPreviousByUser(User user);

}
