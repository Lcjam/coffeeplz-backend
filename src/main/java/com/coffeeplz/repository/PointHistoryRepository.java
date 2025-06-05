package com.coffeeplz.repository;

import com.coffeeplz.entity.PointHistory;
import com.coffeeplz.entity.PointTransactionType;
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
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    /**
     * 특정 사용자의 포인트 내역 조회 (최신순)
     */
    List<PointHistory> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 특정 사용자의 포인트 내역 페이징 조회
     */
    Page<PointHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * 특정 사용자의 특정 타입 포인트 내역 조회
     */
    List<PointHistory> findByUserAndTransactionTypeOrderByCreatedAtDesc(User user, PointTransactionType transactionType);
    
    /**
     * 특정 기간의 포인트 내역 조회
     */
    @Query("SELECT ph FROM PointHistory ph WHERE ph.user = :user AND ph.createdAt BETWEEN :startDate AND :endDate ORDER BY ph.createdAt DESC")
    List<PointHistory> findByUserAndCreatedAtBetween(@Param("user") User user,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * 특정 사용자의 포인트 적립 총합 조회
     */
    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph WHERE ph.user = :user AND ph.transactionType = 'EARNED'")
    Integer getTotalEarnedPointsByUser(@Param("user") User user);
    
    /**
     * 특정 사용자의 포인트 사용 총합 조회
     */
    @Query("SELECT COALESCE(SUM(ph.points), 0) FROM PointHistory ph WHERE ph.user = :user AND ph.transactionType = 'USED'")
    Integer getTotalUsedPointsByUser(@Param("user") User user);
} 