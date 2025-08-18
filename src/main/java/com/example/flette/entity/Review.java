package com.example.flette.entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review") // ⭐ DB 테이블명 매핑
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 맞추기
    @Column(name = "review_id") // DB 컬럼명 매핑
    private Integer reviewId;

    @Column(name = "bouquet_code")
    private Integer bouquetCode;

    @Column(name = "product_id")  // ⭐ 중요
    private Integer productId;

    @Column(name = "score")
    private Integer score;

    @Column(name = "writer")
    private String writer;

    @Column(name = "review_content")
    private String reviewContent;

    @Column(name = "review_image")
    private String reviewImage;

    @Column(name = "review_date")
    @Temporal(TemporalType.TIMESTAMP) // DATETIME 매핑
    private Date reviewDate;

    @Column(name = "luv")
    private Integer luv;
}
