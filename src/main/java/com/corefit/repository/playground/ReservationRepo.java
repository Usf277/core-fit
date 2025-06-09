package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import com.corefit.entity.playground.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByPlaygroundAndDate(Playground playground, LocalDate date);
}
