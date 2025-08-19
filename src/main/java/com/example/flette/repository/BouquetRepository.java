package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.flette.entity.Bouquet;
import java.util.Optional;

@Repository 
public interface BouquetRepository extends JpaRepository<Bouquet, Integer> {
    Optional<Bouquet> findByBouquetCode(int bouquetCode);
}
