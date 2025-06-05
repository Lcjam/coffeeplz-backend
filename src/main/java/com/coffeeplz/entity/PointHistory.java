package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@jakarta.persistence.Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long id;

    @NotNull(message = "사용자는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "거래 타입은 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private PointTransactionType transactionType;

    @NotNull(message = "포인트 금액은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "포인트 금액은 0보다 커야 합니다")
    @Column(name = "points", nullable = false, precision = 10, scale = 2)
    private BigDecimal points;

    @NotNull(message = "거래 후 잔액은 필수입니다")
    @DecimalMin(value = "0.0", message = "거래 후 잔액은 0 이상이어야 합니다")
    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Size(max = 200, message = "설명은 200자 이하여야 합니다")
    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "order_id")
    private Long orderId;

    // 비즈니스 메서드
    public void updateDescription(String description) {
        this.description = description;
    }

    public boolean isCharge() {
        return transactionType == PointTransactionType.CHARGE;
    }

    public boolean isUse() {
        return transactionType == PointTransactionType.USE;
    }

    public boolean isRefund() {
        return transactionType == PointTransactionType.REFUND;
    }

    public boolean isReward() {
        return transactionType == PointTransactionType.REWARD;
    }
} 