package com.example.flette.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; // @Table 어노테이션 추가
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "review") 
public class Review {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Integer reviewId;
	
	private Integer bouquetCode;
	private Integer productId;
	private Integer score;
	private String writer;
	private String reviewContent;
	private String reviewImage;
	private Date reviewDate;
	private Integer luv;
}
