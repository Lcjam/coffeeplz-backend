package com.coffeeplz.controller;

import com.coffeeplz.dto.*;
import com.coffeeplz.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "메뉴 관리", description = "메뉴 및 카테고리 관리 API")
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;

    // ===== 고객용 API =====

    @Operation(summary = "전체 메뉴 조회", description = "판매 가능한 모든 메뉴를 조회합니다 (고객용)")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAvailableMenus() {
        log.info("판매 가능한 메뉴 조회 요청");
        
        List<MenuResponse> response = menuService.getAllMenus();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리별 메뉴 조회", description = "특정 카테고리의 메뉴를 조회합니다")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusByCategory(@PathVariable Long categoryId) {
        log.info("카테고리별 메뉴 조회 요청: {}", categoryId);
        
        List<MenuResponse> response = menuService.getMenusByCategory(categoryId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 검색", description = "메뉴 이름으로 검색합니다")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> searchMenus(@RequestParam String keyword) {
        log.info("메뉴 검색 요청: {}", keyword);
        
        List<MenuResponse> response = menuService.searchMenus(keyword);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "인기 메뉴 조회", description = "인기 메뉴를 조회합니다 (최근 30일 기준)")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getPopularMenus(@RequestParam(defaultValue = "10") int limit) {
        log.info("인기 메뉴 조회 요청: limit={}", limit);
        
        List<MenuResponse> response = menuService.getPopularMenus(limit);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ===== 관리자용 API =====

    @Operation(summary = "관리자용 메뉴 목록 조회", description = "모든 메뉴를 페이징으로 조회합니다 (관리자용)")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<MenuResponse>>> getAllMenusForAdmin(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("관리자용 메뉴 목록 조회 요청");
        
        Page<MenuResponse> response = menuService.getAllMenusForAdmin(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 상세 조회", description = "특정 메뉴의 상세 정보를 조회합니다")
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenu(@PathVariable Long menuId) {
        log.info("메뉴 상세 조회 요청: {}", menuId);
        
        MenuResponse response = menuService.getMenuById(menuId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 생성", description = "새로운 메뉴를 생성합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        log.info("메뉴 생성 요청: {}", request.getName());
        
        MenuResponse response = menuService.createMenu(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("메뉴가 생성되었습니다", response));
    }

    @Operation(summary = "메뉴 수정", description = "기존 메뉴 정보를 수정합니다")
    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuCreateRequest request) {
        log.info("메뉴 수정 요청: {} - {}", menuId, request.getName());
        
        MenuResponse response = menuService.updateMenu(menuId, request);
        
        return ResponseEntity.ok(ApiResponse.success("메뉴가 수정되었습니다", response));
    }

    @Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다")
    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<String>> deleteMenu(@PathVariable Long menuId) {
        log.info("메뉴 삭제 요청: {}", menuId);
        
        menuService.deleteMenu(menuId);
        
        return ResponseEntity.ok(ApiResponse.success("메뉴가 삭제되었습니다"));
    }

    @Operation(summary = "메뉴 품절 상태 변경", description = "메뉴의 품절 상태를 변경합니다")
    @PatchMapping("/{menuId}/availability")
    public ResponseEntity<ApiResponse<String>> updateMenuAvailability(
            @PathVariable Long menuId,
            @RequestParam boolean isAvailable) {
        log.info("메뉴 품절 상태 변경 요청: {} -> {}", menuId, isAvailable);
        
        menuService.updateMenuAvailability(menuId, isAvailable);
        
        return ResponseEntity.ok(ApiResponse.success("메뉴 상태가 변경되었습니다"));
    }

    // ===== 카테고리 관리 API =====

    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리를 조회합니다")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        log.info("카테고리 목록 조회 요청");
        
        List<CategoryResponse> response = menuService.getAllCategories();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다")
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        log.info("카테고리 생성 요청: {}", request.getName());
        
        CategoryResponse response = menuService.createCategory(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리가 생성되었습니다", response));
    }

    @Operation(summary = "카테고리별 메뉴 개수 조회", description = "특정 카테고리의 메뉴 개수를 조회합니다")
    @GetMapping("/categories/{categoryId}/count")
    public ResponseEntity<ApiResponse<Long>> getMenuCountByCategory(@PathVariable Long categoryId) {
        log.info("카테고리별 메뉴 개수 조회 요청: {}", categoryId);
        
        long count = menuService.getMenuCountByCategory(categoryId);
        
        return ResponseEntity.ok(ApiResponse.success("조회 완료", count));
    }
} 