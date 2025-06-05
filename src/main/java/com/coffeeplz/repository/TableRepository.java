package com.coffeeplz.repository;

import com.coffeeplz.entity.Table;
import com.coffeeplz.entity.TableStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {

    // QR코드로 테이블 조회
    Optional<Table> findByQrCode(String qrCode);

    // 테이블 번호로 조회
    Optional<Table> findByTableNumber(String tableNumber);

    // 활성화된 테이블 조회
    List<Table> findByIsActiveTrue();

    // 상태별 테이블 조회
    List<Table> findByStatus(TableStatus status);

    // 활성화되고 사용 가능한 테이블 조회
    List<Table> findByIsActiveTrueAndStatus(TableStatus status);

    // 좌석 수 기준 테이블 조회 (이상)
    List<Table> findBySeatCountGreaterThanEqualAndIsActiveTrue(Integer seatCount);

    // 페이징을 통한 전체 테이블 조회
    Page<Table> findAll(Pageable pageable);

    // 활성화된 테이블 페이징 조회
    Page<Table> findByIsActiveTrue(Pageable pageable);

    // 테이블 번호 중복 체크 (자신 제외)
    @Query("SELECT COUNT(t) FROM Table t WHERE t.tableNumber = :tableNumber AND t.id != :id")
    long countByTableNumberAndIdNot(@Param("tableNumber") String tableNumber, @Param("id") Long id);

    // 테이블 번호 중복 체크 (신규)
    boolean existsByTableNumber(String tableNumber);

    // QR코드 중복 체크 (자신 제외)
    @Query("SELECT COUNT(t) FROM Table t WHERE t.qrCode = :qrCode AND t.id != :id")
    long countByQrCodeAndIdNot(@Param("qrCode") String qrCode, @Param("id") Long id);

    // QR코드 중복 체크 (신규)
    boolean existsByQrCode(String qrCode);

    // 사용 중인 테이블 수 조회
    @Query("SELECT COUNT(t) FROM Table t WHERE t.status = 'OCCUPIED' AND t.isActive = true")
    long countOccupiedTables();

    // 사용 가능한 테이블 수 조회
    @Query("SELECT COUNT(t) FROM Table t WHERE t.status = 'AVAILABLE' AND t.isActive = true")
    long countAvailableTables();

    // 총 좌석 수 조회 (활성화된 테이블)
    @Query("SELECT COALESCE(SUM(t.seatCount), 0) FROM Table t WHERE t.isActive = true")
    long getTotalSeatCount();

    // 위치별 테이블 검색
    @Query("SELECT t FROM Table t WHERE t.locationDescription LIKE %:location% AND t.isActive = true")
    List<Table> findByLocationContaining(@Param("location") String location);
} 