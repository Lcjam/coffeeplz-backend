package com.coffeeplz.repository;

import com.coffeeplz.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, Long> {

    /**
     * 특정 주문 아이템의 옵션들 조회
     */
    List<OrderItemOption> findByOrderItemId(Long orderItemId);

    /**
     * 특정 주문의 모든 아이템 옵션들 조회
     */
    @Query("SELECT oio FROM OrderItemOption oio JOIN oio.orderItem oi WHERE oi.order.id = :orderId")
    List<OrderItemOption> findByOrderId(@Param("orderId") Long orderId);

    /**
     * 특정 메뉴 옵션이 사용된 주문 아이템 옵션들 조회
     */
    List<OrderItemOption> findByMenuOptionId(Long menuOptionId);

    /**
     * 특정 주문 아이템의 옵션 개수 조회
     */
    int countByOrderItemId(Long orderItemId);

    /**
     * 인기 옵션 통계 (특정 기간)
     */
    @Query("SELECT oio.menuOption.id, oio.menuOption.name, COUNT(oio), SUM(oio.quantity) " +
           "FROM OrderItemOption oio JOIN oio.orderItem oi JOIN oi.order o " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oio.menuOption.id, oio.menuOption.name " +
           "ORDER BY COUNT(oio) DESC")
    List<Object[]> getPopularOptionsStats(@Param("startDate") java.time.LocalDateTime startDate, 
                                         @Param("endDate") java.time.LocalDateTime endDate);
} 