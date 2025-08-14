package com.example.flette.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	//Optional<Answer> findByQuestionId(Integer questionId);
	
	// 🚨 메서드 이름을 findByQuestion_QuestionId로 수정합니다.
    // "question" 필드에서 "questionId" 필드를 찾으라는 의미입니다.
    Optional<Answer> findByQuestion_QuestionId(Integer questionId);
    
    // 질문과 연관된 답변을 삭제하는 메서드
    void deleteByQuestion_QuestionId(Integer questionId);
}