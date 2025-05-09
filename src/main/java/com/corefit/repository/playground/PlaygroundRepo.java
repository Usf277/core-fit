package com.corefit.repository.playground;

import com.corefit.entity.playground.Playground;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaygroundRepo extends JpaRepository<Playground, Integer> {

    Optional<Playground> findById(long id);

    Page<Playground> findAllByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Playground p WHERE " +
            "(:search IS NULL OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Playground> findAllByFilters(@Param("search") String search, Pageable pageable);
}
