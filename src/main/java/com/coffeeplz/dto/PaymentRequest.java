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
public class PaymentRequest {
    
    @NotNull(message = "주문 ID는 필수입니다")
    private Long orderId;
    
    @NotBlank(message = "결제 방법은 필수입니다")
    @Size(max = 50, message = "결제 방법은 50자를 초과할 수 없습니다")
    private String paymentMethod; // CARD, CASH, MOBILE
    
    @NotNull(message = "결제 금액은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "결제 금액은 0보다 커야 합니다")
    private BigDecimal amount;
} 