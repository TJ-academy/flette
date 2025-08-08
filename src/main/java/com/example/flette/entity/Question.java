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
public class Question {
	@Id
	private Integer questionId;
	
	private Integer productId;
	private String userid;
	private String title;
	private String content;
	private boolean status;
	private Date questionDate;
}
