package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {
    
    @NotBlank(message = "주문 상태는 필수입니다")
    private String status; // PENDING, PREPARING, READY, COMPLETED, CANCELLED
    
    private String adminNote;
} 