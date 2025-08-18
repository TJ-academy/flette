package com.example.flette.repository;

import com.example.flette.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // 상품별 리뷰 조회
    List<Review> findByProductId(Integer productId);

    // 사용자별 리뷰 리스트 조회
    List<Review> findByWriter(String writer);

    // 사용자별 리뷰 개수 조회
    long countByWriter(String writer);

    // 특정 bouquetCode에 리뷰가 존재하는지 확인
    boolean existsByBouquetCode(Integer bouquetCode);
}
