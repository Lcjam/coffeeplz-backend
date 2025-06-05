package com.coffeeplz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("대기중"),
    PREPARING("준비중"),
    READY("준비완료"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String description;
} 