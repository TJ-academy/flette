package com.example.flette.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Answer {
	@Id
	private Integer answerId;
	
	@Column(name = "qeustionId")
	private Integer questionId;
	
	private String answerContent;
	private Date answerDate;
}
