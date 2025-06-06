package com.coffeeplz.repository;

import com.coffeeplz.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 활성화된 카테고리 목록을 표시 순서대로 조회
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 활성화된 카테고리 페이징 조회
     */
    Page<Category> findByIsActiveTrueOrderByDisplayOrderAsc(Pageable pageable);

    /**
     * 모든 카테고리를 표시 순서대로 조회 (관리자용)
     */
    List<Category> findAllByOrderByDisplayOrderAsc();

    /**
     * 카테고리명으로 검색
     */
    Optional<Category> findByNameAndIsActiveTrue(String name);

    /**
     * 카테고리에 속한 메뉴 수와 함께 조회
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.menus WHERE c.isActive = true ORDER BY c.displayOrder")
    List<Category> findActiveCategoriesWithMenus();

    /**
     * 특정 표시 순서보다 큰 카테고리들 조회
     */
    List<Category> findByDisplayOrderGreaterThanOrderByDisplayOrderAsc(Integer displayOrder);

    /**
     * 카테고리명 중복 확인
     */
    boolean existsByNameAndIsActiveTrue(String name);
} 