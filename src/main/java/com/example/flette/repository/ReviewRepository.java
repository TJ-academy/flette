package com.example.flette.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	List<Review> findByProductId(Integer productId);
}