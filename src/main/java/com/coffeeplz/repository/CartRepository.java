package com.coffeeplz.repository;

import com.coffeeplz.entity.Cart;
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
     * 사용자의 장바구니 조회
     */
    Optional<Cart> findByUser(User user);
    
    /**
     * 사용자의 장바구니 존재 여부 확인
     */
    boolean existsByUser(User user);
    
    /**
     * 사용자의 장바구니 삭제
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user = :user")
    void deleteByUser(@Param("user") User user);
} 