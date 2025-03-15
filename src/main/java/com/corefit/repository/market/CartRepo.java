package com.corefit.repository.market;

import com.corefit.entity.market.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    @Override
    Optional<Cart> findById(Long id);

    @Query("SELECT c FROM Cart c WHERE c.user.id = :id")
    Cart findByUserId(Long id);
}
