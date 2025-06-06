package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private TableResponse table;
    private List<OrderItemResponse> orderItems;
    private String orderType;
    private String status;
    private BigDecimal totalAmount;
    private String customerNote;
    private PaymentResponse payment;
    private LocalDateTime orderTime;
    private LocalDateTime completedTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 