package com.example.flette.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QnaItemDTO {
	private Integer questionId;
	private String title;
	private String writerMasked; // guy**** 형태
	private boolean answered; // true/false
	private LocalDateTime questionDate;
	private String questionContent;

	// answer (nullable)
	private String answerContent;
	private LocalDateTime answerDate;
}
