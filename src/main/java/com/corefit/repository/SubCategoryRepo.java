package com.corefit.repository;

import com.corefit.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepo extends JpaRepository<SubCategory, Long> {
    @Override
    Optional<SubCategory> findById(Long id);

    @Query(value = "SELECT * FROM sub_categories WHERE market_id = :marketId", nativeQuery = true)
    List<SubCategory> getSubCategoriesByMarketId(@Param("marketId") Long marketId);
}
