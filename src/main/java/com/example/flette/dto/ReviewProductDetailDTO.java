package com.example.flette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
public class ReviewProductDetailDTO {
    // Bouquet 및 Product 정보
    private Integer bouquetCode;
    private Integer productId;
    private String productName;
    private String imageName; // 상품 대표 이미지 (bouquet 테이블의 productId로 Product에서 가져옴)
    private Integer totalMoney; // 해당 꽃다발의 총 금액 (bouquet 테이블의 total_money)

    // Main Flower 정보
    private Integer mainACode;
    private String mainAFlowerName;
    private Integer mainBCode;
    private String mainBFlowerName;
    private Integer mainCCode;
    private String mainCFlowerName;

    // 주문 정보 (리뷰 작성 시 필요할 수 있는 연관 정보)
    private Integer orderId;
    private LocalDateTime orderDate;
    private String userId; // 주문자 ID

    // 기타 (필요시 추가)
    // private Integer reviewId; // 기존 리뷰가 있다면 ID
    // private Integer score; // 기존 리뷰가 있다면 점수
    // private String reviewContent; // 기존 리뷰가 있다면 내용
}