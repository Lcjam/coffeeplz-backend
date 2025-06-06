package com.coffeeplz.repository;

import com.coffeeplz.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    /**
     * 판매 가능한 메뉴만 조회
     */
    List<Menu> findByAvailableTrue();
    
    /**
     * 카테고리별 메뉴 조회 (엔티티 기반)
     */
    List<Menu> findByCategoryIdAndIsAvailableTrue(Long categoryId);

    /**
     * 카테고리별 메뉴 조회 (기존 문자열 기반 - 호환성)
     */
    List<Menu> findByCategoryAndAvailableTrue(String category);
    
    /**
     * 가격 범위로 메뉴 조회
     */
    List<Menu> findByPriceBetweenAndAvailableTrue(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 메뉴명으로 검색 (부분 일치)
     */
    @Query("SELECT m FROM Menu m WHERE m.name LIKE %:name% AND m.available = true")
    List<Menu> findByNameContainingAndAvailableTrue(@Param("name") String name);
    
        /**
     * 카테고리별 페이징 조회 (엔티티 기반)
     */
    Page<Menu> findByCategoryIdAndIsAvailableTrue(Long categoryId, Pageable pageable);

    /**
     * 카테고리별 페이징 조회 (기존 문자열 기반)
     */
    Page<Menu> findByCategoryAndAvailableTrue(String category, Pageable pageable);

    /**
     * 메뉴 옵션과 함께 조회
     */
    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.menuOptions WHERE m.id = :menuId AND m.isAvailable = true")
    Menu findByIdWithOptionsAndIsAvailableTrue(@Param("menuId") Long menuId);

    /**
     * 카테고리의 사용 가능한 메뉴 개수 조회
     */
    int countByCategoryIdAndIsAvailableTrue(Long categoryId);

    /**
     * 인기 메뉴 조회 (주문 수 기준)
     */
    @Query("SELECT m, COUNT(oi) as orderCount FROM Menu m " +
           "JOIN m.orderItems oi JOIN oi.order o " +
           "WHERE o.createdAt >= :startDate AND m.isAvailable = true " +
           "GROUP BY m.id ORDER BY orderCount DESC")
    List<Object[]> findPopularMenus(@Param("startDate") java.time.LocalDateTime startDate, Pageable pageable);
} 