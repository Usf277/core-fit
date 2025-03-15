package com.corefit.repository.market;

import com.corefit.entity.market.Product;
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
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:marketId IS NULL OR p.market.id = :marketId) AND " +
            "(:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId)")
    Page<Product> findAllByFilters(Long marketId, Long subCategoryId, String name, Pageable pageable);

}
