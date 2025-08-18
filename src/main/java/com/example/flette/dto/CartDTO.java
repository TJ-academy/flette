package com.example.flette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
	private Integer cartId;

	private Integer bouquetCode;
    private String userid;

    private Integer price;
    private Integer quantity;
    private Integer totalPrice;
}