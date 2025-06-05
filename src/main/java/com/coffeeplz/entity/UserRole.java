package com.coffeeplz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER("ROLE_CUSTOMER", "고객"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
} 