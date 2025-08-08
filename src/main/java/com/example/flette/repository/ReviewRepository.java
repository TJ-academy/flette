package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

}
