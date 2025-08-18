package com.example.flette.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProductDTO {
	private Integer productId;
	private String productName;
	private String imageName;
	private Integer basicPrice;
	private String summary;
	private String description;
	
	public ProductDTO(Integer productId, String productName,
            String imageName, Integer basicPrice,
            String summary, String description) {
		this.productId = productId;
		this.productName = productName;
		this.imageName = imageName;
		this.basicPrice = basicPrice;
		this.summary = summary;
		this.description = description;
		}
}