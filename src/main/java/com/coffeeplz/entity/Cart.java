package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @NotNull(message = "테이블은 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;

    // 연관관계
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    // 비즈니스 메서드
    public void addCartItem(CartItem cartItem) {
        // 같은 메뉴가 이미 있는지 확인
        CartItem existingItem = findCartItemByMenu(cartItem.getMenu());
        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + cartItem.getQuantity());
        } else {
            cartItems.add(cartItem);
            cartItem.setCart(this);
        }
    }

    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCart(null);
    }

    public void removeCartItemByMenu(Menu menu) {
        cartItems.removeIf(item -> item.getMenu().equals(menu));
    }

    public void clearCart() {
        cartItems.clear();
    }

    public BigDecimal getTotalAmount() {
        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getTotalItemCount() {
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    private CartItem findCartItemByMenu(Menu menu) {
        return cartItems.stream()
                .filter(item -> item.getMenu().equals(menu))
                .findFirst()
                .orElse(null);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isTableCart() {
        return table != null;
    }

    public boolean isUserCart() {
        return user != null;
    }
} 