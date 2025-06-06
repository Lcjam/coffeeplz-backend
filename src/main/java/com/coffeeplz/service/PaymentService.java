package com.coffeeplz.service;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.*;
import com.coffeeplz.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 카드 결제 처리
     */
    @Transactional
    public PaymentResponse processCardPayment(Long orderId, BigDecimal amount) {
        log.info("카드 결제 처리 시작 - 주문ID: {}, 결제금액: {}", orderId, amount);

        // 주문 조회 및 검증
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        validatePaymentRequest(order, amount);

        // 결제 정보 생성
        String transactionId = generateTransactionId();
        
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod("CARD")
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .transactionId(transactionId)
                .build();

        payment = paymentRepository.save(payment);

        // 실제 결제 처리 (외부 PG사 연동 시뮬레이션)
        boolean paymentSuccess = processExternalCardPayment();

        if (paymentSuccess) {
            payment.completePayment(transactionId);
            order.updateStatus(OrderStatus.PREPARING);
            log.info("카드 결제 성공 - 거래ID: {}, 주문ID: {}", transactionId, orderId);
        } else {
            payment.failPayment("결제 승인 실패");
            log.warn("카드 결제 실패 - 거래ID: {}, 주문ID: {}", transactionId, orderId);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return convertToPaymentResponse(payment);
    }

    /**
     * 현금 결제 처리
     */
    @Transactional
    public PaymentResponse processCashPayment(Long orderId, BigDecimal amount) {
        log.info("현금 결제 처리 시작 - 주문ID: {}, 결제금액: {}", orderId, amount);

        // 주문 조회 및 검증
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        validatePaymentRequest(order, amount);

        String transactionId = generateTransactionId();

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod("CASH")
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .transactionId(transactionId)
                .build();

        payment.completePayment(transactionId);
        order.updateStatus(OrderStatus.PREPARING);

        paymentRepository.save(payment);
        orderRepository.save(order);

        log.info("현금 결제 완료 - 거래ID: {}, 주문ID: {}", transactionId, orderId);
        return convertToPaymentResponse(payment);
    }

    /**
     * 결제 조회
     */
    public PaymentResponse getPayment(Long paymentId) {
        log.info("결제 조회 - 결제ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        return convertToPaymentResponse(payment);
    }

    /**
     * 주문별 결제 조회
     */
    public PaymentResponse getPaymentByOrder(Long orderId) {
        log.info("주문별 결제 조회 - 주문ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        return convertToPaymentResponse(payment);
    }

    /**
     * 결제 환불
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String reason) {
        log.info("결제 환불 요청 - 결제ID: {}, 사유: {}", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        if (!payment.canRefund()) {
            throw new IllegalArgumentException("환불할 수 없는 결제 상태입니다");
        }

        // 외부 PG사 환불 처리 시뮬레이션
        boolean refundSuccess = processRefundPayment(payment);

        if (refundSuccess) {
            payment.refundPayment();
            payment.setFailureReason("환불: " + reason);
            
            // 주문 상태도 취소로 변경
            Order order = payment.getOrder();
            order.updateStatus(OrderStatus.CANCELLED);
            
            paymentRepository.save(payment);
            orderRepository.save(order);
            
            log.info("결제 환불 완료 - 거래ID: {}", payment.getTransactionId());
        } else {
            log.warn("결제 환불 실패 - 거래ID: {}", payment.getTransactionId());
            throw new RuntimeException("결제 환불 처리 중 오류가 발생했습니다");
        }

        return convertToPaymentResponse(payment);
    }

    /**
     * 오늘 결제 통계
     */
    public String getTodayPaymentStats() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        log.info("오늘 결제 통계 조회: {} ~ {}", startOfDay, endOfDay);

        BigDecimal totalAmount = paymentRepository.getTotalCompletedAmountBetween(startOfDay, endOfDay);
        long totalCount = paymentRepository.getCompletedPaymentCountBetween(startOfDay, endOfDay);

        if (totalAmount == null) totalAmount = BigDecimal.ZERO;

        return String.format("오늘 결제: %d건, 총액: %s원", totalCount, totalAmount.toString());
    }

    /**
     * 결제 요청 유효성 검증
     */
    private void validatePaymentRequest(Order order, BigDecimal amount) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("결제할 수 없는 주문 상태입니다");
        }

        if (!order.getPaymentAmount().equals(amount)) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다");
        }

        // 이미 결제된 주문인지 확인
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            Payment existingPayment = paymentRepository.findByOrderId(order.getId()).get();
            if (existingPayment.getStatus() == PaymentStatus.COMPLETED) {
                throw new IllegalArgumentException("이미 결제 완료된 주문입니다");
            }
        }
    }

    /**
     * 거래 ID 생성
     */
    private String generateTransactionId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TXN" + timestamp + uuid;
    }

    /**
     * 외부 카드 결제 처리 시뮬레이션
     */
    private boolean processExternalCardPayment() {
        // 실제 환경에서는 PG사 API 호출
        // 시뮬레이션: 95% 성공률
        return Math.random() < 0.95;
    }

    /**
     * 외부 결제 환불 처리 시뮬레이션
     */
    private boolean processRefundPayment(Payment payment) {
        // 실제 환경에서는 PG사 환불 API 호출
        // 시뮬레이션: 98% 성공률
        return Math.random() < 0.98;
    }

    /**
     * Payment를 PaymentResponse로 변환
     */
    private PaymentResponse convertToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .paymentTime(payment.getPaymentTime())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }
} 