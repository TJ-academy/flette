package com.example.flette.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
	@Id
	private Integer reviewId;
	
	private Integer bouquetCode;
	private Integer productId;
	private Integer score;
	private String writer;
	private String reviewContent;
	private String reviewImage;
	private Date reviewDate;
	private Integer like;
}
