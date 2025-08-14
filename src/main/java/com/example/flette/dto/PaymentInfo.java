package com.example.flette.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // 이 어노테이션을 추가하여 알 수 없는 필드는 무시하도록 설정
public class PaymentInfo {
    private String imp_uid;
    private String merchant_uid;
    private String status; // 결제 상태: paid(결제완료), ready(미결제), cancelled(취소) 등
    private int amount; // 실제 결제 금액
    private String buyer_name;
    private String buyer_tel;
    private String buyer_email;
    private String name; // 주문 상품명
}
