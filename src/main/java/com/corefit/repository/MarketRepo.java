package com.corefit.repository;

import com.corefit.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketRepo extends JpaRepository<Market, Long> {

    @Override
    Optional<Market> findById(Long id);
}
