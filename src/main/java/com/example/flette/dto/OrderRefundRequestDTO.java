package com.example.flette.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRefundRequestDTO {
    private String refundReason; // 환불 사유
    private String account; // 환불 받을 계좌번호
    private String bank; // 은행명
}