package com.corefit.repository;

import com.corefit.entity.Order;
import com.corefit.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    List<Order> findByUser_Id(Long userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ('ORDER_DELIVERED', 'ORDER_CANCELED')")
    List<Order> findPreviousOrdersByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status NOT IN ('ORDER_RECEIVED', 'ORDER_CONFIRMED', 'ORDER_UNDER_PREPARATION' , 'ORDER_UNDER_DELIVERY')")
    List<Order> findActiveOrdersByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.market.id = :marketId AND o.status = :status")
    List<Order> findMarketOrdersByStatus(Long marketId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.market.id = :marketId AND o.status IN :statuses")
    List<Order> findMarketOrdersByStatuses(Long marketId, List<OrderStatus> statuses);
}
