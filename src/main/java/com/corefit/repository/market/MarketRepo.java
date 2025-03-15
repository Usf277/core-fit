package com.corefit.repository.market;

import com.corefit.entity.market.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketRepo extends JpaRepository<Market, Long> {

    @Override
    Optional<Market> findById(Long id);

    @Query("SELECT m FROM Market m WHERE " +
            "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR m.category.id = :categoryId)")
    Page<Market> findAllByFilters(String name, Long categoryId, Pageable pageable);


    @Query("SELECT COUNT(r) FROM Rate r WHERE r.market.id = :marketId")
    Long getMarketRateCount(Long marketId);

    @Query("SELECT COALESCE(AVG(r.rate), 0) FROM Rate r WHERE r.market.id = :marketId")
    Double getMarketAverageRate(Long marketId);

}

