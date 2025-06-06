package com.coffeeplz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("대기"),
    COMPLETED("완료"),
    FAILED("실패"),
    REFUNDED("환불");

    private final String description;
} 