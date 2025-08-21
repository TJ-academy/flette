package com.example.flette.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")   // ✅ 매핑
    private Integer orderId;

    @Column(name = "userid")     // ✅ 그대로 매핑
    private String userid;

    @Column(name = "money")
    private Integer money;

    @Column(name = "delivery")
    private Integer delivery;

    @Column(name = "total_money")  // ✅ snake_case
    private Integer totalMoney;

    @Column(name = "receiver")
    private String receiver;
    
    @Column(name = "order_address")
    private String orderAddress;
    
    private String tel;

    @CreationTimestamp
    @Column(name = "order_date")
    private LocalDateTime orderDate;  // ✅ Date → LocalDateTime 변경 추천

    @Column(name = "method")
    private String method;

    @Column(name = "bank")
    private String bank;

    @Column(name = "account")
    private String account;

    @Column(name = "status")
    private String status;

    @Column(name = "merchant_uid")   // ✅ snake_case
    private String merchantUid;

    @Column(name = "imp_uid")        // ✅ snake_case
    private String impUid;

    @Column(name = "refund_reason")  // ✅ snake_case
    private String refundReason;
}
