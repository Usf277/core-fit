package com.corefit.repository;

import com.corefit.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {
    @Override
    Optional<CartItem> findById(Long id);
}
