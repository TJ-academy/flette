package com.example.flette.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
	@Id
	private int cartId;
	
	private String userid;
	private String bouquetCode;
	private Integer amount; //수량
	private Integer totalMoney; //총 금액
}
