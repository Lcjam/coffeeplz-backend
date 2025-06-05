package com.coffeeplz.repository;

import com.coffeeplz.entity.Menu;
import com.coffeeplz.entity.Review;
import com.coffeeplz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 특정 메뉴의 리뷰 조회 (최신순)
     */
    List<Review> findByMenuOrderByCreatedAtDesc(Menu menu);
    
    /**
     * 특정 메뉴의 리뷰 페이징 조회
     */
    Page<Review> findByMenuOrderByCreatedAtDesc(Menu menu, Pageable pageable);
    
    /**
     * 특정 사용자의 리뷰 조회
     */
    List<Review> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 특정 사용자가 특정 메뉴에 대해 작성한 리뷰 조회
     */
    Optional<Review> findByUserAndMenu(User user, Menu menu);
    
    /**
     * 사용자가 특정 메뉴에 리뷰를 작성했는지 확인
     */
    boolean existsByUserAndMenu(User user, Menu menu);
    
    /**
     * 특정 메뉴의 평균 평점 조회
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menu = :menu")
    Double getAverageRatingByMenu(@Param("menu") Menu menu);
    
    /**
     * 특정 메뉴의 리뷰 개수 조회
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.menu = :menu")
    Long countByMenu(@Param("menu") Menu menu);
    
    /**
     * 평점별 리뷰 조회
     */
    List<Review> findByMenuAndRatingOrderByCreatedAtDesc(Menu menu, Integer rating);
} 