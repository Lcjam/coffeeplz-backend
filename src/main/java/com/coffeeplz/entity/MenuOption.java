package com.coffeeplz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_options")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_option_id")
    private Long id;

    @NotNull(message = "메뉴는 필수입니다")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @NotBlank(message = "옵션명은 필수입니다")
    @Size(max = 50, message = "옵션명은 50자를 초과할 수 없습니다")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
    @Column(name = "description", length = 200)
    private String description;

    @DecimalMin(value = "0.0", message = "추가 가격은 0 이상이어야 합니다")
    @Column(name = "additional_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private Boolean isRequired = false;

    @Column(name = "max_selections", nullable = false)
    @Builder.Default
    private Integer maxSelections = 1;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    // 연관관계
    @OneToMany(mappedBy = "menuOption", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemOption> orderItemOptions = new ArrayList<>();

    // 비즈니스 메서드
    public void updateOptionInfo(String name, String description, BigDecimal additionalPrice) {
        this.name = name;
        this.description = description;
        this.additionalPrice = additionalPrice;
    }

    public void updateSelectionRule(Boolean isRequired, Integer maxSelections) {
        this.isRequired = isRequired;
        this.maxSelections = maxSelections;
    }

    public void makeAvailable() {
        this.isAvailable = true;
    }

    public void makeUnavailable() {
        this.isAvailable = false;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public boolean isRequired() {
        return this.isRequired;
    }
} 