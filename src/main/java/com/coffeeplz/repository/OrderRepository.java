package com.coffeeplz.repository;

import com.coffeeplz.entity.Order;
import com.coffeeplz.entity.OrderStatus;
import com.coffeeplz.entity.Table;
import com.coffeeplz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 특정 사용자의 주문 내역 조회 (최신순)
     */
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 특정 사용자의 주문 내역 페이징 조회
     */
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * 주문 상태별 조회
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * 특정 사용자의 특정 상태 주문 조회
     */
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    /**
     * 특정 기간의 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * 관리자용: 모든 주문 조회 (최신순)
     */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 관리자용: 상태별 주문 조회 (페이징)
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
    
    /**
     * 테이블별 주문 내역 조회 (최신순)
     */
    List<Order> findByTableOrderByCreatedAtDesc(Table table);
    
    /**
     * 테이블별 주문 내역 페이징 조회
     */
    Page<Order> findByTableOrderByCreatedAtDesc(Table table, Pageable pageable);
    
    /**
     * 테이블의 특정 상태 주문 조회
     */
    List<Order> findByTableAndStatus(Table table, OrderStatus status);
    
    /**
     * 현재 사용 중인 테이블의 진행 중인 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.table = :table AND o.status IN ('PENDING', 'PREPARING', 'READY')")
    List<Order> findActiveOrdersByTable(@Param("table") Table table);
    
    /**
     * QR코드로 테이블의 진행 중인 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.table.qrCode = :qrCode AND o.status IN ('PENDING', 'PREPARING', 'READY')")
    List<Order> findActiveOrdersByQrCode(@Param("qrCode") String qrCode);
    
    /**
     * 테이블별 오늘 주문 수 조회
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.table = :table AND DATE(o.createdAt) = CURRENT_DATE")
    long countTodayOrdersByTable(@Param("table") Table table);
    
    /**
     * 테이블 주문과 사용자 주문 구분 조회
     */
    @Query("SELECT o FROM Order o WHERE o.table IS NOT NULL ORDER BY o.createdAt DESC")
    List<Order> findTableOrdersOrderByCreatedAtDesc();
    
    @Query("SELECT o FROM Order o WHERE o.user IS NOT NULL ORDER BY o.createdAt DESC")
    List<Order> findUserOrdersOrderByCreatedAtDesc();
} 