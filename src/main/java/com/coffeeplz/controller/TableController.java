package com.coffeeplz.controller;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.TableStatus;
import com.coffeeplz.service.TableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "테이블 관리", description = "QR 테이블 스캔 및 관리 API")
@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Slf4j
public class TableController {

    private final TableService tableService;

    // ===== 고객용 API =====

    @Operation(summary = "QR 코드 스캔", description = "QR 코드를 스캔하여 테이블 정보를 조회합니다")
    @GetMapping("/scan/{qrCode}")
    public ResponseEntity<ApiResponse<QrScanResponse>> scanQrCode(@PathVariable String qrCode) {
        log.info("QR 코드 스캔 요청: {}", qrCode);
        
        QrScanResponse response = tableService.getTableByQrCode(qrCode);
        
        return ResponseEntity.ok(ApiResponse.success("QR 스캔이 완료되었습니다", response));
    }

    // ===== 관리자용 API =====

    @Operation(summary = "테이블 목록 조회", description = "모든 테이블 목록을 조회합니다 (관리자용)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAllTables() {
        log.info("테이블 목록 조회 요청");
        
        List<TableResponse> response = tableService.getAllTablesForDashboard();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테이블 상세 조회", description = "특정 테이블의 상세 정보를 조회합니다")
    @GetMapping("/{tableId}")
    public ResponseEntity<ApiResponse<TableResponse>> getTable(@PathVariable Long tableId) {
        log.info("테이블 상세 조회 요청: {}", tableId);
        
        TableResponse response = tableService.getTableById(tableId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "테이블 생성", description = "새로운 테이블을 생성합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<TableResponse>> createTable(@Valid @RequestBody TableCreateRequest request) {
        log.info("테이블 생성 요청: {}", request);
        
        TableResponse response = tableService.createTable(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("테이블이 생성되었습니다", response));
    }

    @Operation(summary = "테이블 수정", description = "기존 테이블 정보를 수정합니다")
    @PutMapping("/{tableId}")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(
            @PathVariable Long tableId,
            @Valid @RequestBody TableCreateRequest request) {
        log.info("테이블 수정 요청: {} - {}", tableId, request);
        
        TableResponse response = tableService.updateTable(tableId, request);
        
        return ResponseEntity.ok(ApiResponse.success("테이블이 수정되었습니다", response));
    }

    @Operation(summary = "테이블 삭제", description = "테이블을 삭제합니다")
    @DeleteMapping("/{tableId}")
    public ResponseEntity<ApiResponse<String>> deleteTable(@PathVariable Long tableId) {
        log.info("테이블 삭제 요청: {}", tableId);
        
        tableService.deleteTable(tableId);
        
        return ResponseEntity.ok(ApiResponse.success("테이블이 삭제되었습니다"));
    }

    @Operation(summary = "테이블 상태 변경", description = "테이블의 상태를 변경합니다 (AVAILABLE, OCCUPIED, MAINTENANCE)")
    @PatchMapping("/{tableId}/status")
    public ResponseEntity<ApiResponse<String>> updateTableStatus(
            @PathVariable Long tableId,
            @RequestParam TableStatus status) {
        log.info("테이블 상태 변경 요청: {} -> {}", tableId, status);
        
        tableService.updateTableStatus(tableId, status);
        
        return ResponseEntity.ok(ApiResponse.success("테이블 상태가 변경되었습니다"));
    }

    @Operation(summary = "테이블 통계 조회", description = "테이블 사용 통계를 조회합니다")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<String>> getTableStats() {
        log.info("테이블 통계 조회 요청");
        
        long occupiedCount = tableService.getOccupiedTableCount();
        long availableCount = tableService.getAvailableTableCount();
        
        String stats = String.format("사용중: %d개, 사용가능: %d개", occupiedCount, availableCount);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "QR 코드 재생성", description = "테이블의 QR 코드를 새로 생성합니다")
    @PostMapping("/{tableId}/regenerate-qr")
    public ResponseEntity<ApiResponse<String>> regenerateQrCode(@PathVariable Long tableId) {
        log.info("QR 코드 재생성 요청: {}", tableId);
        
        String newQrCode = tableService.regenerateQrCode(tableId);
        
        return ResponseEntity.ok(ApiResponse.success("QR 코드가 재생성되었습니다", newQrCode));
    }
} 