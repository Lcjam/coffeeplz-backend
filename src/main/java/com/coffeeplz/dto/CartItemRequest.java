package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequest {
    
    @NotNull(message = "테이블 ID는 필수입니다")
    private Long tableId;
    
    @NotNull(message = "메뉴 ID는 필수입니다")
    private Long menuId;
    
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private int quantity;
    
    private List<Long> selectedOptionIds;
    private String specialInstructions;
} 