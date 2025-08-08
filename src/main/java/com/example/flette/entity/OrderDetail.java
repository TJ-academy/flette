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
public class OrderDetail {
	@Id
	private Integer detailId;
	
	private Integer orderId;
	private Integer bouquetCode;
	private Integer money;
}
