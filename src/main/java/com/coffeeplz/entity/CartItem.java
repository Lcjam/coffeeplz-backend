package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @NotNull(message = "장바구니는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @NotNull(message = "메뉴는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "단가는 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "단가는 0보다 커야 합니다")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "소계는 필수입니다")
    @DecimalMin(value = "0.0", message = "소계는 0 이상이어야 합니다")
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "notes", length = 200)
    private String notes;

    // 비즈니스 메서드
    public void calculateSubtotal() {
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    @PrePersist
    @PreUpdate
    private void calculateSubtotalBeforeSave() {
        calculateSubtotal();
    }
} 