package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @NotNull(message = "주문은 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

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

    // 연관관계
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemOption> orderItemOptions = new ArrayList<>();

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