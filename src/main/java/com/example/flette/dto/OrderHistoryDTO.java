package com.example.flette.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class OrderHistoryDTO {
	private int orderId;
	private Date orderDate;
	private String status;
	private List<OrderDetailDTO> details;
}
