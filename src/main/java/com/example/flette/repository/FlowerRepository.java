package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Flower;

public interface FlowerRepository  extends JpaRepository<Flower, Integer> {
}
