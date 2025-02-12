package com.corefit.repository;

import com.corefit.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    @Override
    Optional<Product> findById(Long id);

    @Query("SELECT p FROM Product p WHERE " +
            "(:marketId IS NULL OR p.market.id = :marketId) AND " +
            "(:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId)")
    Page<Product> findAllByFilters(Pageable pageable, Long marketId, Long subCategoryId);

}
