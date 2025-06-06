package com.coffeeplz.repository;

import com.coffeeplz.entity.Payment;
import com.coffeeplz.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문 ID로 결제 정보 조회
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * 거래 ID로 결제 정보 조회
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * 특정 상태의 결제 목록 조회
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    /**
     * 특정 기간의 결제 목록 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentTime BETWEEN :startDate AND :endDate ORDER BY p.paymentTime DESC")
    List<Payment> findByPaymentTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 결제 방법의 결제 목록 조회
     */
    Page<Payment> findByPaymentMethodOrderByCreatedAtDesc(String paymentMethod, Pageable pageable);

    /**
     * 성공한 결제들의 총 금액 계산
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentTime BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCompletedAmountBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 기간의 결제 건수 조회
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentTime BETWEEN :startDate AND :endDate")
    long getCompletedPaymentCountBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 결제 방법별 통계
     */
    @Query("SELECT p.paymentMethod, COUNT(p), SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentTime BETWEEN :startDate AND :endDate GROUP BY p.paymentMethod")
    List<Object[]> getPaymentStatsByMethod(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
} 