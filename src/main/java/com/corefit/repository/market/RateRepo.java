package com.corefit.repository.market;

import com.corefit.entity.market.Rate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateRepo extends JpaRepository<Rate, Long> {

    @Override
    Optional<Rate> findById(Long aLong);

    @Query("SELECT rate FROM Rate rate WHERE rate.market.id = :marketId")
    Page<Rate> getRatesByMarketId(Long marketId, Pageable pageable);

}
