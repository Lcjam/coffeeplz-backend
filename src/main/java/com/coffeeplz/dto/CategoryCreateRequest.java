package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateRequest {
    
    @NotBlank(message = "카테고리명은 필수입니다")
    @Size(max = 50, message = "카테고리명은 50자를 초과할 수 없습니다")
    private String name;
    
    @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
    private String description;
    
    @Builder.Default
    private Integer displayOrder = 0;
} 