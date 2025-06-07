package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CafeTable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long id;

    @NotBlank(message = "테이블 번호는 필수입니다")
    @Column(name = "table_number", nullable = false, unique = true, length = 10)
    private String tableNumber;

    @NotNull(message = "테이블 좌석 수는 필수입니다")
    @Min(value = 1, message = "좌석 수는 1 이상이어야 합니다")
    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;

    @NotBlank(message = "QR코드는 필수입니다")
    @Column(name = "qr_code", nullable = false, unique = true, length = 100)
    private String qrCode;

    @Column(name = "location_description", length = 200)
    private String locationDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 연관관계
    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Cart> carts = new ArrayList<>();

    // 비즈니스 메서드
    public void updateTableInfo(String tableNumber, Integer seatCount, String locationDescription) {
        this.tableNumber = tableNumber;
        this.seatCount = seatCount;
        this.locationDescription = locationDescription;
    }

    public void updateQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void occupy() {
        if (status != TableStatus.AVAILABLE) {
            throw new IllegalStateException("사용할 수 없는 테이블입니다");
        }
        this.status = TableStatus.OCCUPIED;
    }

    public void makeAvailable() {
        this.status = TableStatus.AVAILABLE;
    }

    public void setMaintenance() {
        this.status = TableStatus.MAINTENANCE;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isAvailable() {
        return isActive && status == TableStatus.AVAILABLE;
    }

    public boolean isOccupied() {
        return status == TableStatus.OCCUPIED;
    }
} 