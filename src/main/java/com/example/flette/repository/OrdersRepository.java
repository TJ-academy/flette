package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.flette.entity.Orders;

public interface OrdersRepository  extends JpaRepository<Orders, Integer>, JpaSpecificationExecutor<Orders>  {

	// 해당 userid로 주문 내역 개수를 조회
    long countByUserid(String userid);
}
