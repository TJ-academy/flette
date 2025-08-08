package com.example.flette.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
	@Id
	private Integer orderId;
	
	private String userid;
	private Integer money;
	private Integer delivery;
	private Integer totalMoney;
	private String orderAddress;
	private Date orderDate;
	private String method;
	private String bank;
	private Integer account;
	private String status;
	private String merchantUid;
	private String impUid;
	private String refundReason;
}
