package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.example.flette.entity.Orders;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Integer>, JpaSpecificationExecutor<Orders> {
    // AdminApi에서 사용하는 spec 기반 검색 그대로 동작
    // MyPageApi 전용 메서드 추가
    List<Orders> findByUserid(String userid);
    int countByUserid(String userid);
}
