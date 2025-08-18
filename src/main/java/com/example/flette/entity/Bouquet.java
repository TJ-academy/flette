package com.example.flette.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bouquet {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer bouquetCode;
	
	private Integer mainA, mainB, mainC;
	private Integer subA, subB, subC;
	private Integer leafA, leafB, leafC;
	private Integer wrapping;
	private Integer addA, addB, addC;
	private Integer productId;
	private Integer totalMoney;
}
