package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Orders;

public interface OrdersRepository  extends JpaRepository<Orders, Integer> {

}
