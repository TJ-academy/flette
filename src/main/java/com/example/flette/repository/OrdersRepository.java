package com.example.flette.repository;

import com.example.flette.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByMerchantUid(String merchantUid);
    long countByUserid(String userid); 
}
