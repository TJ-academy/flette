package com.example.flette.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	List<Review> findByProductId(Integer productId);
	
	// 해당 userid로 작성된 리뷰 개수를 조회
    long countByWriter(String writer);

    // 해당 bouquetCode로 작성된 리뷰가 있는지 확인하는 메서드 추가
    boolean existsByBouquetCode(Integer bouquetCode);
}