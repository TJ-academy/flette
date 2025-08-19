package com.example.flette.dto;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrdersDTO {
	private Integer orderId;
	
	private String userid; 
	private String money;
	private Integer delivery;
	private Integer totalMoney; 
	private String orderAddress;
	
	private Date orderDate;
	
	private String method;
	private String bank;
	private String account;
	private String status;
	
	private String merchantUid; 
	private String impUid; 
	private String refundReason; 
}
