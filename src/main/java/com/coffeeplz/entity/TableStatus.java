package com.coffeeplz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TableStatus {
    AVAILABLE("사용 가능", "available"),
    OCCUPIED("사용 중", "occupied"),
    MAINTENANCE("정비 중", "maintenance");

    private final String description;
    private final String key;
} 