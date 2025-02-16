package com.corefit.repository;

import com.corefit.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    @Override
    Optional<Cart> findById(Long id);

    @Query("SELECT c FROM Cart c WHERE c.user.id = :id")
    Optional<Cart> findByUserId(Long id);
}
