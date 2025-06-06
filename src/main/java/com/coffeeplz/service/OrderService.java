package com.coffeeplz.service;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.*;
import com.coffeeplz.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final TableRepository tableRepository;
    private final CartService cartService;

    /**
     * 장바구니에서 주문 생성
     */
    @Transactional
    public OrderResponse createOrderFromCart(Long tableId, String customerNotes) {
        log.info("주문 생성 시작 - 테이블: {}", tableId);

        // 테이블 유효성 검증
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        if (table.getStatus() != TableStatus.OCCUPIED) {
            throw new IllegalArgumentException("사용중이 아닌 테이블입니다");
        }

        // 장바구니 조회 및 검증
        Cart cart = cartRepository.findByTableWithItems(table)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 비어있습니다"));

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("장바구니에 아이템이 없습니다");
        }

        // 주문 생성
        Order order = Order.builder()
                .table(table)
                .totalAmount(cart.getTotalAmount())
                .paymentAmount(cart.getTotalAmount())
                .status(OrderStatus.PENDING)
                .orderNotes(customerNotes)
                .build();

        Order savedOrder = orderRepository.save(order);

        // 장바구니 아이템을 주문 아이템으로 변환
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> convertCartItemToOrderItem(cartItem, savedOrder))
                .toList();

        orderItemRepository.saveAll(orderItems);

        // 장바구니 삭제
        cartService.clearCart(tableId);

        log.info("주문 생성 완료 - 주문ID: {}, 테이블: {}, 총액: {}", 
                savedOrder.getId(), table.getTableNumber(), savedOrder.getTotalAmount());

        return convertToOrderResponse(savedOrder);
    }

    /**
     * 주문 조회
     */
    public OrderResponse getOrder(Long orderId) {
        log.info("주문 조회 - 주문ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        return convertToOrderResponse(order);
    }

    /**
     * 테이블의 진행 중인 주문 조회
     */
    public List<OrderResponse> getActiveOrdersByTable(Long tableId) {
        log.info("테이블 진행 중인 주문 조회 - 테이블: {}", tableId);

        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        List<Order> orders = orderRepository.findActiveOrdersByTable(table);

        return orders.stream()
                .map(this::convertToOrderResponse)
                .toList();
    }

    /**
     * 주문 상태 변경 (관리자용)
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("주문 상태 변경 - 주문ID: {}, 새상태: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        // 상태별 처리
        switch (newStatus) {
            case PREPARING -> {
                order.prepare();
                log.info("주문 조리 시작 - 주문ID: {}", order.getId());
            }
            case READY -> {
                order.ready();
                log.info("주문 준비 완료 - 주문ID: {}", order.getId());
            }
            case COMPLETED -> {
                order.complete();
                log.info("주문 완료 - 주문ID: {}", order.getId());
            }
            case CANCELLED -> {
                order.cancel();
                log.info("주문 취소 - 주문ID: {}", order.getId());
            }
            default -> {
                order.updateStatus(newStatus);
            }
        }

        orderRepository.save(order);
        return convertToOrderResponse(order);
    }

    /**
     * 주문 취소 (고객용)
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        log.info("주문 취소 요청 - 주문ID: {}, 사유: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        // 취소 가능 상태인지 확인
        if (!order.canCancel()) {
            throw new IllegalArgumentException("현재 상태에서는 주문을 취소할 수 없습니다");
        }

        order.cancel();
        order.updateOrderNotes(order.getOrderNotes() + " [취소사유: " + reason + "]");

        orderRepository.save(order);

        log.info("주문 취소 완료 - 주문ID: {}", order.getId());
        return convertToOrderResponse(order);
    }

    /**
     * 관리자용 주문 목록 조회
     */
    public Page<OrderResponse> getOrdersForAdmin(OrderStatus status, LocalDateTime startDate, 
                                               LocalDateTime endDate, Pageable pageable) {
        log.info("관리자 주문 목록 조회 - 상태: {}, 기간: {} ~ {}", status, startDate, endDate);

        Page<Order> orders;
        
        if (status != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return orders.map(this::convertToOrderResponse);
    }

    /**
     * 오늘 주문 통계 (간단한 메서드)
     */
    public String getTodayOrderStats() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        log.info("오늘 주문 통계 조회: {} ~ {}", startOfDay, endOfDay);

        List<Order> todayOrders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        
        long totalOrders = todayOrders.size();
        long completedOrders = todayOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();
        
        BigDecimal totalRevenue = todayOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return String.format("오늘 주문: %d건, 완료: %d건, 매출: %s원", 
                totalOrders, completedOrders, totalRevenue.toString());
    }

    /**
     * 주문 상태별 개수 조회 (관리자 대시보드용)
     */
    public String getOrderStatusCounts() {
        log.info("주문 상태별 개수 조회");

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        List<Order> preparingOrders = orderRepository.findByStatus(OrderStatus.PREPARING);
        List<Order> readyOrders = orderRepository.findByStatus(OrderStatus.READY);
        List<Order> completedOrders = orderRepository.findByStatus(OrderStatus.COMPLETED);
        List<Order> cancelledOrders = orderRepository.findByStatus(OrderStatus.CANCELLED);

        return String.format("대기: %d, 조리중: %d, 준비완료: %d, 완료: %d, 취소: %d",
                pendingOrders.size(), preparingOrders.size(), readyOrders.size(),
                completedOrders.size(), cancelledOrders.size());
    }

    /**
     * 장바구니 아이템을 주문 아이템으로 변환
     */
    private OrderItem convertCartItemToOrderItem(CartItem cartItem, Order order) {
        return OrderItem.builder()
                .order(order)
                .menu(cartItem.getMenu())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .subtotal(cartItem.getSubtotal())
                .notes(cartItem.getNotes())
                .build();
    }

    /**
     * Order를 OrderResponse로 변환
     */
    private OrderResponse convertToOrderResponse(Order order) {
        TableResponse tableResponse = TableResponse.builder()
                .id(order.getTable().getId())
                .tableNumber(order.getTable().getTableNumber())
                .seatCount(order.getTable().getSeatCount())
                .locationDescription(order.getTable().getLocationDescription())
                .status(order.getTable().getStatus().name())
                .qrCode(order.getTable().getQrCode())
                .build();

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::convertToOrderItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .table(tableResponse)
                .orderItems(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .customerNote(order.getOrderNotes())
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * OrderItem을 OrderItemResponse로 변환
     */
    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        Menu menu = orderItem.getMenu();
        
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

        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .menu(menuResponse)
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getSubtotal())
                .specialInstructions(orderItem.getNotes())
                .build();
    }
} 