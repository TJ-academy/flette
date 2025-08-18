package com.example.flette.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QnADTO {
	private Integer questionId;
	private Integer productId;
	private String userid;
	private String title;
	private String content;
	private boolean status;
	private String passwd;
	private LocalDateTime questionDate;
	
	private Integer answerId;
	private String answerContent;
	private LocalDateTime answerDate;
}