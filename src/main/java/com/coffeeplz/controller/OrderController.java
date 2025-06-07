package com.coffeeplz.controller;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.OrderStatus;
import com.coffeeplz.service.OrderService;
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
import java.util.Map;

@Tag(name = "주문 관리", description = "주문 생성, 조회, 상태 관리 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // ===== 고객용 API =====

    @Operation(summary = "주문 생성", description = "장바구니 기반으로 주문을 생성합니다")
    @PostMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @PathVariable Long tableId,
            @RequestParam(required = false) String customerNotes) {
        log.info("주문 생성 요청: 테이블 {}, 고객 메모: {}", tableId, customerNotes);
        
        OrderResponse response = orderService.createOrderFromCart(tableId, customerNotes);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("주문이 생성되었습니다", response));
    }

    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 정보를 조회합니다")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        log.info("주문 상세 조회 요청: {}", orderId);
        
        OrderResponse response = orderService.getOrder(orderId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테이블별 활성 주문 조회", description = "특정 테이블의 활성 주문들을 조회합니다")
    @GetMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getActiveOrdersByTable(@PathVariable Long tableId) {
        log.info("테이블별 활성 주문 조회 요청: 테이블 {}", tableId);
        
        List<OrderResponse> response = orderService.getActiveOrdersByTable(tableId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 취소", description = "주문을 취소합니다")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        log.info("주문 취소 요청: {}, 사유: {}", orderId, reason);
        
        OrderResponse response = orderService.cancelOrder(orderId, reason);
        
        return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다", response));
    }

    // ===== 관리자용 API =====

    @Operation(summary = "관리자용 주문 목록 조회", description = "관리자용 주문 목록을 조회합니다")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersForAdmin(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("관리자용 주문 목록 조회 요청: status={}", status);
        
        // 실제로는 날짜 파싱이 필요하지만 임시로 null로 처리
        Page<OrderResponse> response = orderService.getOrdersForAdmin(status, null, null, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 상태 변경", description = "주문의 상태를 변경합니다")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {
        log.info("주문 상태 변경 요청: {} -> {}", orderId, newStatus);
        
        OrderResponse response = orderService.updateOrderStatus(orderId, newStatus);
        
        return ResponseEntity.ok(ApiResponse.success("주문 상태가 변경되었습니다", response));
    }

    @Operation(summary = "오늘 주문 통계", description = "오늘의 주문 통계를 조회합니다")
    @GetMapping("/stats/today")
    public ResponseEntity<ApiResponse<String>> getTodayOrderStats() {
        log.info("오늘 주문 통계 조회 요청");
        
        String stats = orderService.getTodayOrderStats();
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "상태별 주문 개수 조회", description = "각 상태별 주문 개수를 조회합니다")
    @GetMapping("/stats/status-count")
    public ResponseEntity<ApiResponse<String>> getOrderStatusCounts() {
        log.info("상태별 주문 개수 조회 요청");
        
        String statusCounts = orderService.getOrderStatusCounts();
        
        return ResponseEntity.ok(ApiResponse.success(statusCounts));
    }
} 