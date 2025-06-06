package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableCreateRequest {
    
    @NotBlank(message = "테이블 번호는 필수입니다")
    @Size(max = 10, message = "테이블 번호는 10자를 초과할 수 없습니다")
    private String tableNumber;
    
    @NotNull(message = "좌석 수는 필수입니다")
    @Min(value = 1, message = "좌석 수는 1 이상이어야 합니다")
    private Integer seatCount;
    
    @Size(max = 200, message = "위치 설명은 200자를 초과할 수 없습니다")
    private String locationDescription;
} 