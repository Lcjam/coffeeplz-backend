package com.coffeeplz.controller;

import com.coffeeplz.dto.*;
import com.coffeeplz.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "장바구니 관리", description = "테이블별 익명 장바구니 관리 API")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 조회", description = "특정 테이블의 장바구니를 조회합니다")
    @GetMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long tableId) {
        log.info("장바구니 조회 요청: 테이블 {}", tableId);
        
        CartResponse response = cartService.getCart(tableId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "장바구니에 메뉴 추가", description = "장바구니에 메뉴 아이템을 추가합니다")
    @PostMapping("/table/{tableId}/items")
    public ResponseEntity<ApiResponse<CartResponse>> addCartItem(
            @PathVariable Long tableId,
            @Valid @RequestBody CartItemRequest request) {
        log.info("장바구니 아이템 추가 요청: 테이블 {}, 메뉴 {}, 수량 {}", 
                tableId, request.getMenuId(), request.getQuantity());
        
        CartResponse response = cartService.addItemToCart(tableId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("장바구니에 추가되었습니다", response));
    }

    @Operation(summary = "장바구니 아이템 수량 변경", description = "장바구니 아이템의 수량을 변경합니다")
    @PutMapping("/table/{tableId}/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItemQuantity(
            @PathVariable Long tableId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        log.info("장바구니 아이템 수량 변경 요청: 테이블 {}, 아이템 {} -> 수량 {}", tableId, cartItemId, quantity);
        
        CartResponse response = cartService.updateCartItemQuantity(tableId, cartItemId, quantity);
        
        return ResponseEntity.ok(ApiResponse.success("수량이 변경되었습니다", response));
    }

    @Operation(summary = "장바구니 아이템 삭제", description = "장바구니에서 특정 아이템을 삭제합니다")
    @DeleteMapping("/table/{tableId}/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @PathVariable Long tableId,
            @PathVariable Long cartItemId) {
        log.info("장바구니 아이템 삭제 요청: 테이블 {}, 아이템 {}", tableId, cartItemId);
        
        CartResponse response = cartService.removeCartItem(tableId, cartItemId);
        
        return ResponseEntity.ok(ApiResponse.success("아이템이 삭제되었습니다", response));
    }

    @Operation(summary = "장바구니 전체 비우기", description = "테이블의 장바구니를 전체 비웁니다")
    @DeleteMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long tableId) {
        log.info("장바구니 전체 비우기 요청: 테이블 {}", tableId);
        
        cartService.clearCart(tableId);
        
        return ResponseEntity.ok(ApiResponse.success("장바구니가 비워졌습니다"));
    }

    @Operation(summary = "장바구니 총 금액 조회", description = "테이블 장바구니의 총 금액을 조회합니다")
    @GetMapping("/table/{tableId}/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getCartTotal(@PathVariable Long tableId) {
        log.info("장바구니 총 금액 조회 요청: 테이블 {}", tableId);
        
        BigDecimal totalAmount = cartService.calculateCartTotal(tableId);
        
        return ResponseEntity.ok(ApiResponse.success("총 금액", totalAmount));
    }

    @Operation(summary = "장바구니 활성 상태 확인", description = "테이블에 활성 장바구니가 있는지 확인합니다")
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveCart(@PathVariable Long tableId) {
        log.info("장바구니 활성 상태 확인 요청: 테이블 {}", tableId);
        
        boolean hasActive = cartService.hasActiveCart(tableId);
        
        return ResponseEntity.ok(ApiResponse.success("활성 장바구니 여부", hasActive));
    }

    @Operation(summary = "빈 장바구니 정리", description = "시스템의 빈 장바구니들을 정리합니다 (관리자용)")
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupEmptyCarts() {
        log.info("빈 장바구니 정리 요청");
        
        cartService.cleanupEmptyCarts();
        
        return ResponseEntity.ok(ApiResponse.success("빈 장바구니가 정리되었습니다"));
    }
} 