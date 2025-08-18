package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import com.example.flette.entity.Flower;

public interface FlowerRepository  extends JpaRepository<Flower, Integer>, JpaSpecificationExecutor<Flower> {
    List<Flower> findByCategoryAndShowTrue(String category);
}
