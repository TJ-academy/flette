package com.example.flette.repository;

import com.example.flette.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer>, JpaSpecificationExecutor<Orders> {
    Optional<Orders> findByMerchantUid(String merchantUid);
    long countByUserid(String userid);
    List<Orders> findByUseridOrderByOrderDateDesc(String userid);
}