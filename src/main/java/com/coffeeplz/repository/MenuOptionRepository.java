package com.coffeeplz.repository;

import com.coffeeplz.entity.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {

    /**
     * 특정 메뉴의 사용 가능한 옵션들 조회
     */
    List<MenuOption> findByMenuIdAndIsAvailableTrue(Long menuId);

    /**
     * 특정 메뉴의 모든 옵션들 조회 (관리자용)
     */
    List<MenuOption> findByMenuIdOrderByCreatedAtAsc(Long menuId);

    /**
     * 특정 메뉴의 필수 옵션들 조회
     */
    List<MenuOption> findByMenuIdAndIsRequiredTrueAndIsAvailableTrue(Long menuId);

    /**
     * 여러 옵션 ID로 조회
     */
    @Query("SELECT mo FROM MenuOption mo WHERE mo.id IN :optionIds AND mo.isAvailable = true")
    List<MenuOption> findByIdInAndIsAvailableTrue(@Param("optionIds") List<Long> optionIds);

    /**
     * 특정 메뉴의 옵션 개수 조회
     */
    int countByMenuIdAndIsAvailableTrue(Long menuId);

    /**
     * 메뉴별 옵션 존재 여부 확인
     */
    boolean existsByMenuIdAndIsAvailableTrue(Long menuId);

    /**
     * 특정 메뉴의 옵션명 중복 확인
     */
    boolean existsByMenuIdAndNameAndIsAvailableTrue(Long menuId, String name);
} 