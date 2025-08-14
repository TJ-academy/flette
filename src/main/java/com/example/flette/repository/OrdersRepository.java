package com.example.flette.repository;

import com.example.flette.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 이 부분을 추가합니다.
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer>, JpaSpecificationExecutor<Orders> { // JpaSpecificationExecutor를 상속받도록 수정합니다.
    Optional<Orders> findByMerchantUid(String merchantUid);
    long countByUserid(String userid);
}