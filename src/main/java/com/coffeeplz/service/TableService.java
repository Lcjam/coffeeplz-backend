package com.coffeeplz.service;

import com.coffeeplz.dto.*;
import com.coffeeplz.entity.Table;
import com.coffeeplz.entity.TableStatus;
import com.coffeeplz.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TableService {

    private final TableRepository tableRepository;

    /**
     * QR 코드로 테이블 정보 조회 (소비자용)
     */
    public QrScanResponse getTableByQrCode(String qrCode) {
        log.info("QR 코드로 테이블 조회: {}", qrCode);
        
        Table table = tableRepository.findByQrCodeAndIsActiveTrue(qrCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 QR 코드입니다"));

        // 테이블 사용 가능 여부 확인
        if (table.getStatus() == TableStatus.MAINTENANCE) {
            throw new IllegalArgumentException("현재 사용할 수 없는 테이블입니다");
        }

        // 테이블 상태를 사용중으로 변경
        if (table.getStatus() == TableStatus.AVAILABLE) {
            table.occupy();
            tableRepository.save(table);
        }

        return QrScanResponse.builder()
                .tableId(table.getId())
                .tableNumber(table.getTableNumber())
                .seatCount(table.getSeatCount())
                .locationDescription(table.getLocationDescription())
                .build();
    }

    /**
     * 테이블 목록 조회 (관리자용)
     */
    public Page<TableResponse> getAllTables(Pageable pageable) {
        Page<Table> tables = tableRepository.findByIsActiveTrue(pageable);
        
        return tables.map(table -> TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .seatCount(table.getSeatCount())
                .locationDescription(table.getLocationDescription())
                .status(table.getStatus().name())
                .qrCode(table.getQrCode())
                .build());
    }

    /**
     * 전체 테이블 목록 조회 (대시보드용)
     */
    public List<TableResponse> getAllTablesForDashboard() {
        List<Table> tables = tableRepository.findByIsActiveTrue();
        
        return tables.stream()
                .map(table -> TableResponse.builder()
                        .id(table.getId())
                        .tableNumber(table.getTableNumber())
                        .seatCount(table.getSeatCount())
                        .locationDescription(table.getLocationDescription())
                        .status(table.getStatus().name())
                        .qrCode(table.getQrCode())
                        .build())
                .toList();
    }

    /**
     * 테이블 상세 조회
     */
    public TableResponse getTableById(Long tableId) {
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .seatCount(table.getSeatCount())
                .locationDescription(table.getLocationDescription())
                .status(table.getStatus().name())
                .qrCode(table.getQrCode())
                .build();
    }

    /**
     * 테이블 추가 (관리자용)
     */
    @Transactional
    public TableResponse createTable(TableCreateRequest request) {
        log.info("테이블 생성 요청: {}", request.getTableNumber());

        // 테이블 번호 중복 체크
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new IllegalArgumentException("이미 존재하는 테이블 번호입니다");
        }

        // QR 코드 생성
        String qrCode = generateUniqueQrCode();

        Table table = Table.builder()
                .tableNumber(request.getTableNumber())
                .seatCount(request.getSeatCount())
                .locationDescription(request.getLocationDescription())
                .qrCode(qrCode)
                .status(TableStatus.AVAILABLE)
                .build();

        Table savedTable = tableRepository.save(table);
        log.info("테이블 생성 완료: {} (QR: {})", savedTable.getTableNumber(), qrCode);

        return TableResponse.builder()
                .id(savedTable.getId())
                .tableNumber(savedTable.getTableNumber())
                .seatCount(savedTable.getSeatCount())
                .locationDescription(savedTable.getLocationDescription())
                .status(savedTable.getStatus().name())
                .qrCode(savedTable.getQrCode())
                .build();
    }

    /**
     * 테이블 정보 수정 (관리자용)
     */
    @Transactional
    public TableResponse updateTable(Long tableId, TableCreateRequest request) {
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        // 테이블 번호 중복 체크 (자신 제외)
        if (!table.getTableNumber().equals(request.getTableNumber()) &&
            tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new IllegalArgumentException("이미 존재하는 테이블 번호입니다");
        }

        table.updateTableInfo(
                request.getTableNumber(),
                request.getSeatCount(),
                request.getLocationDescription()
        );

        Table updatedTable = tableRepository.save(table);
        log.info("테이블 정보 수정 완료: {}", updatedTable.getTableNumber());

        return TableResponse.builder()
                .id(updatedTable.getId())
                .tableNumber(updatedTable.getTableNumber())
                .seatCount(updatedTable.getSeatCount())
                .locationDescription(updatedTable.getLocationDescription())
                .status(updatedTable.getStatus().name())
                .qrCode(updatedTable.getQrCode())
                .build();
    }

    /**
     * 테이블 상태 변경 (관리자용)
     */
    @Transactional
    public void updateTableStatus(Long tableId, TableStatus status) {
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        switch (status) {
            case AVAILABLE -> table.makeAvailable();
            case OCCUPIED -> table.occupy();
            case MAINTENANCE -> table.setMaintenance();
        }
        
        tableRepository.save(table);
        log.info("테이블 상태 변경: {} -> {}", table.getTableNumber(), status);
    }

    /**
     * 테이블 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteTable(Long tableId) {
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        // 사용중인 테이블은 삭제 불가
        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new IllegalArgumentException("사용중인 테이블은 삭제할 수 없습니다");
        }

        table.deactivate();
        tableRepository.save(table);
        log.info("테이블 삭제 완료: {}", table.getTableNumber());
    }

    /**
     * 테이블 정리 (주문 완료 후)
     */
    @Transactional
    public void clearTable(Long tableId) {
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        table.makeAvailable();
        tableRepository.save(table);
        log.info("테이블 정리 완료: {}", table.getTableNumber());
    }

    /**
     * QR 코드 재생성
     */
    @Transactional
    public String regenerateQrCode(Long tableId) {
        Table table = tableRepository.findById(tableId)
                .filter(Table::getIsActive)
                .orElseThrow(() -> new IllegalArgumentException("테이블을 찾을 수 없습니다"));

        String newQrCode = generateUniqueQrCode();
        table.updateQrCode(newQrCode);
        tableRepository.save(table);
        
        log.info("QR 코드 재생성 완료: {} -> {}", table.getTableNumber(), newQrCode);
        return newQrCode;
    }

    /**
     * 유니크한 QR 코드 생성
     */
    private String generateUniqueQrCode() {
        String qrCode;
        do {
            qrCode = "TABLE_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (tableRepository.existsByQrCode(qrCode));
        
        return qrCode;
    }

    /**
     * 테이블 상태별 통계 조회
     */
    public long getOccupiedTableCount() {
        return tableRepository.countOccupiedTables();
    }

    public long getAvailableTableCount() {
        return tableRepository.countAvailableTables();
    }

    public long getTotalSeatCount() {
        return tableRepository.getTotalSeatCount();
    }
} 