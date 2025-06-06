package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @NotNull(message = "테이블은 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private Table table;

    @NotNull(message = "총 금액은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "총 금액은 0보다 커야 합니다")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "사용 포인트는 0 이상이어야 합니다")
    @Column(name = "used_points", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal usedPoints = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "실제 결제 금액은 0 이상이어야 합니다")
    @Column(name = "payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "order_notes", length = 500)
    private String orderNotes;

    // 연관관계
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    // 비즈니스 메서드
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.paymentAmount = this.totalAmount.subtract(this.usedPoints);
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    public void updateOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public void applyPoints(BigDecimal points) {
        this.usedPoints = points;
        this.paymentAmount = this.totalAmount.subtract(points);
    }

    public boolean canCancel() {
        return status == OrderStatus.PENDING;
    }

    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException("취소할 수 없는 주문 상태입니다");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void prepare() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("준비할 수 없는 주문 상태입니다");
        }
        this.status = OrderStatus.PREPARING;
    }

    public void ready() {
        if (status != OrderStatus.PREPARING) {
            throw new IllegalStateException("준비완료로 변경할 수 없는 주문 상태입니다");
        }
        this.status = OrderStatus.READY;
    }

    public void complete() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("완료로 변경할 수 없는 주문 상태입니다");
        }
        this.status = OrderStatus.COMPLETED;
        // 주문 완료 시 테이블을 사용가능 상태로 변경
        if (table != null) {
            table.makeAvailable();
        }
    }

    public void setTable(Table table) {
        this.table = table;
        // 주문 생성 시 테이블을 사용 중으로 변경
        if (table != null) {
            table.occupy();
        }
    }

    public boolean isTableOrder() {
        return table != null;
    }

    public boolean isUserOrder() {
        return user != null;
    }
} 