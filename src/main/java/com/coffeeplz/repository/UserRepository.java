package com.coffeeplz.repository;

import com.coffeeplz.entity.User;
import com.coffeeplz.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 관리자 조회 (관리자 로그인용)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 체크 (관리자 가입용)
     */
    boolean existsByEmail(String email);
    
    /**
     * 역할별 관리자 조회
     */
    List<User> findByRole(UserRole role);
    
    /**
     * 활성화된 관리자 목록 조회
     */
    List<User> findByIsActiveTrueOrderByCreatedAtAsc();
    
    /**
     * 이메일과 역할로 관리자 조회
     */
    Optional<User> findByEmailAndRole(String email, UserRole role);
    
    /**
     * 관리자 통계 조회
     */
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.role")
    List<Object[]> getAdminStatsByRole();
} 