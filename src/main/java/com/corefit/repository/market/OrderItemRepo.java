package com.corefit.repository.market;

import com.corefit.dto.response.market.TopProductStats;
import com.corefit.entity.market.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    @Query("""
                SELECT new com.corefit.dto.response.market.TopProductStats(
                    oi.product.id,
                    oi.product.name,
                    SUM(oi.quantity)
                )
                FROM OrderItem oi
                WHERE oi.order.market.id IN :marketIds
                GROUP BY oi.product.id, oi.product.name
                ORDER BY SUM(oi.quantity) DESC
            """)
    List<TopProductStats> findTopProductsByMarketIds(
            @Param("marketIds") List<Long> marketIds
    );
}
