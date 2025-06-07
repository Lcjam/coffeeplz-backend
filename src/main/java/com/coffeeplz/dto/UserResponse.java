package com.coffeeplz.dto;

import com.coffeeplz.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private UserRole role;
    private Integer points;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 