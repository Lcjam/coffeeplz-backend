package com.coffeeplz.repository;

import com.coffeeplz.entity.Cart;
import com.coffeeplz.entity.CartItem;
import com.coffeeplz.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 특정 장바구니의 모든 아이템 조회
     */
    List<CartItem> findByCart(Cart cart);
    
    /**
     * 장바구니에서 특정 메뉴 아이템 조회
     */
    Optional<CartItem> findByCartAndMenu(Cart cart, Menu menu);
    
    /**
     * 장바구니에 특정 메뉴가 있는지 확인
     */
    boolean existsByCartAndMenu(Cart cart, Menu menu);
    
    /**
     * 특정 장바구니의 모든 아이템 삭제
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteByCart(@Param("cart") Cart cart);
    
    /**
     * 장바구니의 총 아이템 수 조회
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart = :cart")
    Long countByCart(@Param("cart") Cart cart);
    
    /**
     * 장바구니의 총 수량 조회
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart = :cart")
    Integer getTotalQuantityByCart(@Param("cart") Cart cart);
} 