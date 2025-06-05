package com.coffeeplz.repository;

import com.coffeeplz.entity.Order;
import com.coffeeplz.entity.OrderItem;
import com.coffeeplz.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * 특정 주문의 주문 아이템들 조회
     */
    List<OrderItem> findByOrder(Order order);
    
    /**
     * 특정 메뉴가 포함된 주문 아이템들 조회
     */
    List<OrderItem> findByMenu(Menu menu);
    
    /**
     * 인기 메뉴 조회 (주문 수량 기준)
     */
    @Query("SELECT oi.menu, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi " +
           "GROUP BY oi.menu " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findPopularMenus();
    
    /**
     * 특정 메뉴의 총 주문 수량 조회
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.menu = :menu")
    Long getTotalQuantityByMenu(@Param("menu") Menu menu);
} 