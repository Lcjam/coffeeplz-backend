package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOptionCreateRequest {
    
    @NotNull(message = "메뉴 ID는 필수입니다")
    private Long menuId;
    
    @NotBlank(message = "옵션명은 필수입니다")
    @Size(max = 50, message = "옵션명은 50자를 초과할 수 없습니다")
    private String name;
    
    @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
    private String description;
    
    @DecimalMin(value = "0.0", message = "추가 가격은 0 이상이어야 합니다")
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;
    
    @Builder.Default
    private Boolean isRequired = false;
    
    @Builder.Default
    private Integer maxSelections = 1;
} 