package com.coffeeplz.controller;

import com.coffeeplz.dto.*;
import com.coffeeplz.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "결제 관리", description = "결제 처리 및 조회 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "카드 결제 처리", description = "주문에 대한 카드 결제를 처리합니다")
    @PostMapping("/card")
    public ResponseEntity<ApiResponse<PaymentResponse>> processCardPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("카드 결제 처리 요청: 주문 {}, 금액 {}", request.getOrderId(), request.getAmount());
        
        PaymentResponse response = paymentService.processCardPayment(request.getOrderId(), request.getAmount());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("카드 결제가 완료되었습니다", response));
    }

    @Operation(summary = "현금 결제 처리", description = "주문에 대한 현금 결제를 처리합니다")
    @PostMapping("/cash")
    public ResponseEntity<ApiResponse<PaymentResponse>> processCashPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("현금 결제 처리 요청: 주문 {}, 금액 {}", request.getOrderId(), request.getAmount());
        
        PaymentResponse response = paymentService.processCashPayment(request.getOrderId(), request.getAmount());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("현금 결제가 완료되었습니다", response));
    }

    @Operation(summary = "결제 상세 조회", description = "특정 결제의 상세 정보를 조회합니다")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long paymentId) {
        log.info("결제 상세 조회 요청: {}", paymentId);
        
        PaymentResponse response = paymentService.getPayment(paymentId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문별 결제 조회", description = "특정 주문의 결제 정보를 조회합니다")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable Long orderId) {
        log.info("주문별 결제 조회 요청: 주문 {}", orderId);
        
        PaymentResponse response = paymentService.getPaymentByOrder(orderId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 환불", description = "결제를 환불 처리합니다")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason) {
        log.info("결제 환불 요청: {}, 사유: {}", paymentId, reason);
        
        PaymentResponse response = paymentService.refundPayment(paymentId, reason);
        
        return ResponseEntity.ok(ApiResponse.success("환불이 완료되었습니다", response));
    }

    // ===== 관리자용 API =====

    @Operation(summary = "오늘 결제 통계", description = "오늘의 결제 통계를 조회합니다")
    @GetMapping("/stats/today")
    public ResponseEntity<ApiResponse<String>> getTodayPaymentStats() {
        log.info("오늘 결제 통계 조회 요청");
        
        String stats = paymentService.getTodayPaymentStats();
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
} 