package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Product;

public interface ProductRepository  extends JpaRepository<Product, Integer> {
}