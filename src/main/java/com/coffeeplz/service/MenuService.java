package com.coffeeplz.service;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.Category;
import com.coffeeplz.entity.Menu;
import com.coffeeplz.repository.CategoryRepository;
import com.coffeeplz.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 전체 메뉴 조회 (소비자용) - 판매 가능한 메뉴만
     */
    public List<MenuResponse> getAllMenus() {
        List<Menu> menus = menuRepository.findByAvailableTrue();
        
        return menus.stream()
                .map(this::convertToMenuResponse)
                .toList();
    }

    /**
     * 카테고리별 메뉴 조회 (소비자용)
     */
    public List<MenuResponse> getMenusByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        List<Menu> menus = menuRepository.findByCategoryIdAndIsAvailableTrue(categoryId);
        
        return menus.stream()
                .map(this::convertToMenuResponse)
                .toList();
    }

    /**
     * 메뉴 상세 조회 (옵션 포함)
     */
    public MenuResponse getMenuById(Long menuId) {
        Menu menu = menuRepository.findByIdWithOptionsAndIsAvailableTrue(menuId);
        if (menu == null) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다");
        }

        return convertToMenuResponse(menu);
    }

    /**
     * 관리자용 전체 메뉴 조회 (품절 포함)
     */
    public Page<MenuResponse> getAllMenusForAdmin(Pageable pageable) {
        Page<Menu> menus = menuRepository.findAll(pageable);
        
        return menus.map(this::convertToMenuResponse);
    }

    /**
     * 메뉴 추가 (관리자용)
     */
    @Transactional
    public MenuResponse createMenu(MenuCreateRequest request) {
        log.info("메뉴 생성 요청: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        Menu menu = Menu.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .isAvailable(true)
                .build();

        Menu savedMenu = menuRepository.save(menu);
        log.info("메뉴 생성 완료: {} (ID: {})", savedMenu.getName(), savedMenu.getId());

        return convertToMenuResponse(savedMenu);
    }

    /**
     * 메뉴 수정 (관리자용)
     */
    @Transactional
    public MenuResponse updateMenu(Long menuId, MenuCreateRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        menu.updateInfo(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                category
        );

        Menu updatedMenu = menuRepository.save(menu);
        log.info("메뉴 수정 완료: {}", updatedMenu.getName());

        return convertToMenuResponse(updatedMenu);
    }

    /**
     * 메뉴 품절/재고 관리 (관리자용)
     */
    @Transactional
    public void updateMenuAvailability(Long menuId, boolean isAvailable) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다"));

        if (isAvailable) {
            menu.makeAvailable();
        } else {
            menu.makeUnavailable();
        }
        
        menuRepository.save(menu);
        log.info("메뉴 품절 상태 변경: {} -> {}", menu.getName(), isAvailable ? "판매중" : "품절");
    }

    /**
     * 메뉴 삭제 (실제 삭제, 관리자용)
     */
    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다"));

        menuRepository.delete(menu);
        log.info("메뉴 삭제 완료: {}", menu.getName());
    }

    /**
     * 전체 카테고리 조회
     */
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        
        return categories.stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .displayOrder(category.getDisplayOrder())
                        .build())
                .toList();
    }

    /**
     * 카테고리 추가 (관리자용)
     */
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        log.info("카테고리 생성 요청: {}", request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 생성 완료: {}", savedCategory.getName());

        return CategoryResponse.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .description(savedCategory.getDescription())
                .displayOrder(savedCategory.getDisplayOrder())
                .build();
    }

    /**
     * 인기 메뉴 조회 (통계용)
     */
    public List<MenuResponse> getPopularMenus(int limit) {
        // 최근 30일간의 인기 메뉴 조회
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        Pageable pageable = Pageable.ofSize(limit);
        
        List<Object[]> popularMenusData = menuRepository.findPopularMenus(startDate, pageable);
        
        return popularMenusData.stream()
                .map(data -> {
                    Menu menu = (Menu) data[0];
                    return convertToMenuResponse(menu);
                })
                .toList();
    }

    /**
     * 카테고리별 메뉴 개수 조회
     */
    public long getMenuCountByCategory(Long categoryId) {
        return menuRepository.countByCategoryIdAndIsAvailableTrue(categoryId);
    }

    /**
     * 메뉴 검색 (이름 기준)
     */
    public List<MenuResponse> searchMenus(String keyword) {
        List<Menu> menus = menuRepository.findByNameContainingAndAvailableTrue(keyword);
        
        return menus.stream()
                .map(this::convertToMenuResponse)
                .toList();
    }

    /**
     * Menu Entity를 MenuResponse DTO로 변환
     */
    private MenuResponse convertToMenuResponse(Menu menu) {
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(menu.getCategory().getId())
                .name(menu.getCategory().getName())
                .description(menu.getCategory().getDescription())
                .displayOrder(menu.getCategory().getDisplayOrder())
                .build();

        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .available(menu.getIsAvailable())
                .category(categoryResponse)
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
} 