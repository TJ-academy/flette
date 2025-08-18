package com.example.flette.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BouquetDTO {
	private Integer bouquetCode;
	
	private Integer mainA, mainB, mainC;
	private Integer subA, subB, subC;
	private Integer leafA, leafB, leafC;
	private Integer wrapping;
	private Integer addA, addB, addC;
	private Integer productId;
	private Integer totalMoney;
}
