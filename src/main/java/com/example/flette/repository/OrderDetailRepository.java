package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.OrderDetail;

public interface OrderDetailRepository  extends JpaRepository<OrderDetail, Integer> {

}
