package com.example.flette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReviewRequestDTO {
    private Integer bouquetCode;
    private Integer luv; // '좋아요' 기능을 위해 추가된 필드. 임시로 '0'으로 설정
    private Integer productId;
    private String reviewContent;
    private String reviewImage; // 리뷰 이미지 파일명
    private Integer score;
    private String writer; // 작성자 ID
}