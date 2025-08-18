package com.example.flette.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    // 상품별 리뷰
    List<Review> findByProductId(Integer productId);

    // 사용자별 리뷰 리스트
    List<Review> findByWriter(String writer);

    // 사용자별 리뷰 개수
    int countByWriter(String writer);
}