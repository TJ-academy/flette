package com.example.flette.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Answer {
	@Id
	private Integer answerId; // 🚨 @GeneratedValue 제거
	
	@OneToOne
    @MapsId
    @JoinColumn(name = "question_id", referencedColumnName = "question_id", foreignKey = @ForeignKey(name = "FK_ANSWER_QUESTION"))
	private Question question; // 🚨 변수명을 questionId에서 question으로 변경하는 것이 더 자연스럽습니다.
	
	private String answerContent;
	private Date answerDate;
}
