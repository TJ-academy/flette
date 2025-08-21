package com.example.flette.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailBouquetDTO {
	private Integer detailId;
	
	private Integer orderId;
	private Integer bouquetCode;
	private String productName;
	private String imageName;
	
	private Integer money;
	private Integer quantity;
	private Integer totalMoney;
	
	private List<BouquetInfoDTO> bouquetInfoList;
}