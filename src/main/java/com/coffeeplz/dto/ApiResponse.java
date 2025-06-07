package com.coffeeplz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "API 응답 공통 형식")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다")
    private String message;
    @Schema(description = "응답 데이터")
    private T data;
    @Schema(description = "에러 코드", example = "E001")
    private String errorCode;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static ApiResponse<Void> error(String message, String errorCode) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
} 