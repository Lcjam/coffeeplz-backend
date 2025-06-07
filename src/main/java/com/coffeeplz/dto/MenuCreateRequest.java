package com.coffeeplz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "메뉴 생성/수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCreateRequest {
    
    @Schema(description = "메뉴명", example = "아메리카노", required = true)
    @NotBlank(message = "메뉴명은 필수입니다")
    @Size(max = 100, message = "메뉴명은 100자를 초과할 수 없습니다")
    private String name;
    
    @Schema(description = "메뉴 설명", example = "진한 에스프레소에 뜨거운 물을 넣어 만든 커피")
    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    private String description;
    
    @Schema(description = "가격", example = "4500", required = true)
    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    private BigDecimal price;
    
    @Schema(description = "이미지 URL", example = "https://example.com/americano.jpg")
    private String imageUrl;
    
    @Schema(description = "카테고리 ID", example = "1", required = true)
    @NotNull(message = "카테고리 ID는 필수입니다")
    private Long categoryId;
    
    @Schema(description = "판매 가능 여부", example = "true", defaultValue = "true")
    @Builder.Default
    private boolean available = true;
} 