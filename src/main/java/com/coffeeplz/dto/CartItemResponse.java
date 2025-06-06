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
public class CartItemResponse {
    private Long id;
    private MenuResponse menu;
    private int quantity;
    private List<MenuOptionResponse> selectedOptions;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String specialInstructions;
} 