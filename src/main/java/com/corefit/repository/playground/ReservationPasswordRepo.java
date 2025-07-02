package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.ReservationPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationPasswordRepo extends JpaRepository<ReservationPassword, Long> {
    @Query("SELECT rp FROM ReservationPassword rp WHERE rp.reservation.id = :reservationId")
    Optional<ReservationPassword> findByReservationId(Long reservationId);

    @Query("SELECT rp FROM ReservationPassword rp WHERE rp.reservation.playground = :playground AND rp.createdAt >= :dateStart AND rp.createdAt < :dateEnd")
    List<ReservationPassword> findAllByPlaygroundAndDate(Playground playground, LocalDateTime dateStart, LocalDateTime dateEnd);

    @Modifying
    @Query("DELETE FROM ReservationPassword rp WHERE rp.reservation.playground = :playground")
    void deleteAllByPlayground(Playground playground);

    /// Helper method
    default List<ReservationPassword> findAllByPlaygroundAndDate(Playground playground, LocalDate date) {
        return findAllByPlaygroundAndDate(playground, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }
}