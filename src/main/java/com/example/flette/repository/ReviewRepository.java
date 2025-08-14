package com.example.flette.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	List<Review> findByProductId(Integer productId);
	
	// 해당 userid로 작성된 리뷰 개수를 조회
    long countByWriter(String writer);
}