package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.flette.entity.Decoration;

public interface DecorationRepository extends JpaRepository<Decoration, Integer>{
    List<Decoration> findByCategoryAndShowTrue(String category);}
