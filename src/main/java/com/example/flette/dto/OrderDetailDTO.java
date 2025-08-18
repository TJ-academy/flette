package com.example.flette.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrderDetailDTO {
	private Integer detailId;
	private Integer orderId;
	private Integer bouquetCode;
	private Integer money;
}
