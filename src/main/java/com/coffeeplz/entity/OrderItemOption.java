package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "order_item_options")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItemOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_option_id")
    private Long id;

    @NotNull(message = "주문 아이템은 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @NotNull(message = "메뉴 옵션은 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_id", nullable = false)
    private MenuOption menuOption;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    // 비즈니스 메서드
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }
} 