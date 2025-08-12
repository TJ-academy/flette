package com.example.flette.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
	List<Cart> findByUserId(String userId);
}
