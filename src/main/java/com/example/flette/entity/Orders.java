package com.example.flette.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders") // 테이블 이름이 'Orders'가 아닌 'orders'일 경우 매핑
public class Orders {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 설정
	private Integer orderId;
	
	private String userid; 
	private Integer money;
	private Integer delivery;
	private Integer totalMoney; // totalMoney로 변경
	private String orderAddress;
	
    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시간 저장
	private Date orderDate;
	
	private String method;
	private String bank;
	private Integer account;
	private String status;
	
	private String merchantUid; // merchantUid로 변경
	private String impUid; // impUid로 변경
	private String refundReason; // refundReason으로 변경
}
