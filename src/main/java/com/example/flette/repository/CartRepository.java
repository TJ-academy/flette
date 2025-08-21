package com.example.flette.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Cart;
import com.example.flette.entity.Member;

public interface CartRepository extends JpaRepository<Cart, Integer> {
	List<Cart> findByMember(Member member);
	List<Cart> findByMember_UseridOrderByCartIdAsc(String userid);
	void deleteByBouquet_BouquetCode(Integer bouquetCode);
}