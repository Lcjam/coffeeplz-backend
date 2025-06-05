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
     * 카테고리별 메뉴 조회
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
     * 카테고리별 페이징 조회
     */
    Page<Menu> findByCategoryAndAvailableTrue(String category, Pageable pageable);
    
    /**
     * 모든 카테고리 목록 조회
     */
    @Query("SELECT DISTINCT m.category FROM Menu m WHERE m.available = true")
    List<String> findDistinctCategories();
} 