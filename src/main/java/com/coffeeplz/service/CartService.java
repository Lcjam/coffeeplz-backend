package com.coffeeplz.service;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.*;
import com.coffeeplz.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TableRepository tableRepository;
    private final MenuRepository menuRepository;

    /**
     * 장바구니 아이템 추가
     */
    @Transactional
    public CartResponse addItemToCart(Long tableId, CartItemRequest request) {
        log.info("장바구니 아이템 추가 - 테이블: {}, 메뉴: {}", tableId, request.getMenuId());

        // 테이블 조회 및 유효성 검증
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        if (table.getStatus() != TableStatus.OCCUPIED) {
            throw new IllegalArgumentException("사용중이 아닌 테이블입니다");
        }

        // 메뉴 조회 및 유효성 검증
        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다"));

        if (!menu.getIsAvailable()) {
            throw new IllegalArgumentException("현재 판매하지 않는 메뉴입니다");
        }

        // 장바구니 조회 또는 생성
        Cart cart = getOrCreateCart(table);

        // 동일한 메뉴가 이미 장바구니에 있는지 확인
        CartItem existingItem = cartItemRepository.findByCartAndMenu(cart, menu).orElse(null);
        
        if (existingItem != null) {
            // 기존 아이템의 수량 업데이트
            existingItem.updateQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            log.info("기존 장바구니 아이템 수량 업데이트: {} -> {}", 
                    existingItem.getQuantity() - request.getQuantity(), existingItem.getQuantity());
        } else {
            // 새로운 아이템 추가
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(request.getQuantity())
                    .unitPrice(menu.getPrice())
                    .notes(request.getSpecialInstructions())
                    .build();
            
            cartItemRepository.save(cartItem);
            log.info("새로운 장바구니 아이템 추가: {} x{}", menu.getName(), request.getQuantity());
        }

        return getCartResponse(tableId);
    }

    /**
     * 테이블별 장바구니 조회
     */
    public CartResponse getCart(Long tableId) {
        log.info("장바구니 조회 - 테이블: {}", tableId);

        // 테이블 유효성 검증
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        return getCartResponse(tableId);
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @Transactional
    public CartResponse updateCartItemQuantity(Long tableId, Long cartItemId, Integer quantity) {
        log.info("장바구니 아이템 수량 변경 - 테이블: {}, 아이템: {}, 수량: {}", tableId, cartItemId, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }

        // 장바구니 아이템 조회 및 테이블 검증
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다"));

        if (!cartItem.getCart().getTable().getId().equals(tableId)) {
            throw new IllegalArgumentException("해당 테이블의 장바구니 아이템이 아닙니다");
        }

        // 메뉴 유효성 재검증
        if (!cartItem.getMenu().getIsAvailable()) {
            throw new IllegalArgumentException("현재 판매하지 않는 메뉴입니다");
        }

        cartItem.updateQuantity(quantity);
        cartItemRepository.save(cartItem);

        log.info("장바구니 아이템 수량 변경 완료: {} -> {}", cartItem.getMenu().getName(), quantity);
        return getCartResponse(tableId);
    }

    /**
     * 장바구니 아이템 삭제
     */
    @Transactional
    public CartResponse removeCartItem(Long tableId, Long cartItemId) {
        log.info("장바구니 아이템 삭제 - 테이블: {}, 아이템: {}", tableId, cartItemId);

        // 장바구니 아이템 조회 및 테이블 검증
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다"));

        if (!cartItem.getCart().getTable().getId().equals(tableId)) {
            throw new IllegalArgumentException("해당 테이블의 장바구니 아이템이 아닙니다");
        }

        String menuName = cartItem.getMenu().getName();
        cartItemRepository.delete(cartItem);

        log.info("장바구니 아이템 삭제 완료: {}", menuName);
        return getCartResponse(tableId);
    }

    /**
     * 장바구니 전체 삭제
     */
    @Transactional
    public void clearCart(Long tableId) {
        log.info("장바구니 전체 삭제 - 테이블: {}", tableId);

        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        Cart cart = cartRepository.findByTable(table).orElse(null);
        
        if (cart != null) {
            cartRepository.delete(cart);
            log.info("장바구니 전체 삭제 완료 - 테이블: {}", tableId);
        }
    }

    /**
     * 빈 장바구니 정리 (스케줄러용)
     */
    @Transactional
    public void cleanupEmptyCarts() {
        log.info("빈 장바구니 정리 시작");
        cartRepository.deleteEmptyCarts();
        log.info("빈 장바구니 정리 완료");
    }

    /**
     * 테이블별 장바구니 존재 여부 확인
     */
    public boolean hasActiveCart(Long tableId) {
        Table table = tableRepository.findById(tableId).orElse(null);
        if (table == null) {
            return false;
        }
        
        Cart cart = cartRepository.findByTable(table).orElse(null);
        return cart != null && !cart.isEmpty();
    }

    /**
     * 장바구니 총 금액 계산
     */
    public BigDecimal calculateCartTotal(Long tableId) {
        Table table = tableRepository.findById(tableId).orElse(null);
        if (table == null) {
            return BigDecimal.ZERO;
        }
        
        Cart cart = cartRepository.findByTable(table).orElse(null);
        return cart != null ? cart.getTotalAmount() : BigDecimal.ZERO;
    }

    /**
     * 장바구니 조회 또는 생성
     */
    private Cart getOrCreateCart(Table table) {
        Cart cart = cartRepository.findByTable(table).orElse(null);
        
        if (cart == null) {
            // 새로운 장바구니 생성
            cart = Cart.builder()
                    .table(table)
                    .build();
            
            cart = cartRepository.save(cart);
            log.info("새로운 장바구니 생성 - 테이블: {}", table.getTableNumber());
        }
        
        return cart;
    }

    /**
     * 장바구니 응답 DTO 생성
     */
    private CartResponse getCartResponse(Long tableId) {
        Table table = tableRepository.findById(tableId).orElse(null);
        if (table == null) {
            return createEmptyCartResponse(tableId);
        }

        Cart cart = cartRepository.findByTableWithItems(table).orElse(null);
        
        if (cart == null || cart.isEmpty()) {
            return createEmptyCartResponse(tableId);
        }

        List<CartItemResponse> itemResponses = cart.getCartItems().stream()
                .map(this::convertToCartItemResponse)
                .toList();

        TableResponse tableResponse = TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .seatCount(table.getSeatCount())
                .locationDescription(table.getLocationDescription())
                .status(table.getStatus().name())
                .qrCode(table.getQrCode())
                .build();

        return CartResponse.builder()
                .id(cart.getId())
                .table(tableResponse)
                .cartItems(itemResponses)
                .totalAmount(cart.getTotalAmount())
                .createdAt(cart.getCreatedAt())
                .build();
    }

    /**
     * 빈 장바구니 응답 생성
     */
    private CartResponse createEmptyCartResponse(Long tableId) {
        Table table = tableRepository.findById(tableId).orElse(null);
        TableResponse tableResponse = null;
        
        if (table != null) {
            tableResponse = TableResponse.builder()
                    .id(table.getId())
                    .tableNumber(table.getTableNumber())
                    .seatCount(table.getSeatCount())
                    .locationDescription(table.getLocationDescription())
                    .status(table.getStatus().name())
                    .qrCode(table.getQrCode())
                    .build();
        }

        return CartResponse.builder()
                .table(tableResponse)
                .cartItems(List.of())
                .totalAmount(BigDecimal.ZERO)
                .build();
    }

    /**
     * CartItem을 CartItemResponse로 변환
     */
    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        Menu menu = cartItem.getMenu();
        
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(menu.getCategory().getId())
                .name(menu.getCategory().getName())
                .description(menu.getCategory().getDescription())
                .build();
        
        MenuResponse menuResponse = MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .category(categoryResponse)
                .available(menu.getIsAvailable())
                .build();

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .menu(menuResponse)
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getSubtotal())
                .specialInstructions(cartItem.getNotes())
                .build();
    }
} 