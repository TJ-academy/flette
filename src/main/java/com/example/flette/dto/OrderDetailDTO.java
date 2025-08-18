package com.example.flette.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrderDetailDTO {
    // Orders 테이블에서 가져올 정보 (주문 전체 정보)
    private Integer orderId;
    private String impUid;
    private LocalDateTime orderDate;
    private String status;
    private Integer totalMoney;
    private String userid; // 'userId'로 통일하는 것이 좋습니다 (자바 컨벤션)
    private String orderAddress;

    // OrderDetail 테이블에서 가져올 정보 (주문 상세 품목 리스트)
    private List<ProductDetail> details;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class ProductDetail {
        private Integer detailId;
        private Integer bouquetCode;
        private Integer money;
        private String productName;
        private String imageName;
        private boolean hasReview;
    }
}