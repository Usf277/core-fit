package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
import com.corefit.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
