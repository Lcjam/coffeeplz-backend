package com.coffeeplz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointTransactionType {
    CHARGE("충전"),
    USE("사용"),
    REFUND("환불"),
    REWARD("적립");

    private final String description;
} 