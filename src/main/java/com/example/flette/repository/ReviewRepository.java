package com.example.flette.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // 상품별 리뷰 조회
    List<Review> findByProductId(Integer productId);

    // 사용자별 리뷰 리스트 조회
    List<Review> findByWriter(String writer);

    // 사용자별 리뷰 개수 조회
    long countByWriter(String writer);

    // 특정 bouquetCode에 리뷰가 존재하는지 확인
    boolean existsByBouquetCode(Integer bouquetCode);

    // 특정 bouquetCode에 해당하는 리뷰 객체 조회
    Optional<Review> findByBouquetCode(Integer bouquetCode);
    
 // Pagination support for fetching reviews
    Page<Review> findByProductId(Integer productId, Pageable pageable);
    
 // reviewImage가 null이 아닌 것들만 조회
    Page<Review> findByReviewImageIsNotNullOrderByReviewDateDesc(Pageable pageable);
    
    Page<Review> findByReviewImageIsNotNullOrderByLuvDesc(Pageable pageable);
    Page<Review> findByReviewImageIsNotNullOrderByScoreDesc(Pageable pageable);
}
