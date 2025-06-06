package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private MenuResponse menu;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private List<MenuOptionResponse> selectedOptions;
    private String specialInstructions;
} 