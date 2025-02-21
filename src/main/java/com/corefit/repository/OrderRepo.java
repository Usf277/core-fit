package com.corefit.repository;

import com.corefit.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByUser_Id(Long userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND (o.status = 'ORDER_DELIVERED' OR o.status = 'ORDER_CANCELED')")
    List<Order> findPreviousOrdersByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status NOT IN ('ORDER_DELIVERED', 'ORDER_CANCELED')")
    List<Order> findActiveOrdersByUserId(Long userId);
}
