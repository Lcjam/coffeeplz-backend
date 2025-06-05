package com.coffeeplz.repository;

import com.coffeeplz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회 (회원 로그인용)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 체크 (회원 가입용)
     */
    boolean existsByEmail(String email);
    
    /**
     * 특정 포인트 이상을 가진 사용자 조회 (회원 혜택용)
     */
    @Query("SELECT u FROM User u WHERE u.points >= :points")
    java.util.List<User> findByPointsGreaterThanEqual(@Param("points") Integer points);
} 