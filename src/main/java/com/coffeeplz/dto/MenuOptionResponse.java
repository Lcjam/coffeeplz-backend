package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOptionResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal additionalPrice;
    private boolean required;
    private int maxSelections;
    private boolean available;
} 