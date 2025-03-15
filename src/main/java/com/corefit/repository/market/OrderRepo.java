package com.corefit.repository.market;

import com.corefit.entity.market.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ('ORDER_DELIVERED', 'ORDER_CANCELED')")
    List<Order> findPreviousOrdersByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ('ORDER_RECEIVED', 'ORDER_CONFIRMED', 'ORDER_UNDER_PREPARATION', 'ORDER_UNDER_DELIVERY')")
    List<Order> findActiveOrdersByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.market.id = :marketId AND o.status = 'ORDER_RECEIVED'")
    List<Order> findNewMarketOrders(Long marketId);

    @Query("SELECT o FROM Order o WHERE o.market.id = :marketId AND o.status IN ('ORDER_CONFIRMED', 'ORDER_UNDER_PREPARATION', 'ORDER_UNDER_DELIVERY')")
    List<Order> findCurrentMarketOrders(Long marketId);

    @Query("SELECT o FROM Order o WHERE o.market.id = :marketId AND o.status IN ('ORDER_DELIVERED', 'ORDER_CANCELED')")
    List<Order> findCompletedMarketOrders(Long marketId);

}
