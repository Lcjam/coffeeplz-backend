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
public class CartResponse {
    private Long id;
    private TableResponse table;
    private String sessionId;
    private List<CartItemResponse> cartItems;
    private BigDecimal totalAmount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
} 