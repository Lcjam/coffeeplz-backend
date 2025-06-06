package com.coffeeplz.repository;

import com.coffeeplz.entity.Cart;
import com.coffeeplz.entity.Table;
import com.coffeeplz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * 회원 사용자의 장바구니 조회 (회원 전용 기능)
     */
    Optional<Cart> findByUser(User user);
    
    /**
     * 회원 사용자의 장바구니 존재 여부 확인 (회원 전용 기능)
     */
    boolean existsByUser(User user);
    
    /**
     * 회원 사용자의 장바구니 삭제 (회원 전용 기능)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user = :user")
    void deleteByUser(@Param("user") User user);
    
    /**
     * 테이블의 장바구니 조회 (QR 주문 시스템 메인 기능)
     */
    Optional<Cart> findByTable(Table table);
    
    /**
     * QR코드로 테이블의 장바구니 조회 (QR 주문 시스템 메인 기능)
     */
    @Query("SELECT c FROM Cart c WHERE c.table.qrCode = :qrCode")
    Optional<Cart> findByTableQrCode(@Param("qrCode") String qrCode);
    
    /**
     * 테이블의 장바구니 존재 여부 확인
     */
    boolean existsByTable(Table table);
    
    /**
     * 테이블의 장바구니 삭제
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.table = :table")
    void deleteByTable(@Param("table") Table table);
    
    /**
     * QR코드로 테이블의 장바구니 삭제
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.table.qrCode = :qrCode")
    void deleteByTableQrCode(@Param("qrCode") String qrCode);

    /**
     * 테이블의 장바구니를 아이템과 함께 조회
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.menu WHERE c.table = :table")
    Optional<Cart> findByTableWithItems(@Param("table") Table table);

    /**
     * QR코드로 테이블의 장바구니를 아이템과 함께 조회
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.menu WHERE c.table.qrCode = :qrCode")
    Optional<Cart> findByTableQrCodeWithItems(@Param("qrCode") String qrCode);

    /**
     * 빈 장바구니들 삭제 (정리용)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id NOT IN (SELECT DISTINCT ci.cart.id FROM CartItem ci)")
    void deleteEmptyCarts();
} 