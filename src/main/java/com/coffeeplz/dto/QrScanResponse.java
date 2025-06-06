package com.coffeeplz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrScanResponse {
    private Long tableId;
    private String tableNumber;
    private Integer seatCount;
    private String locationDescription;
    private boolean isAvailable;
    private String sessionId;
} 