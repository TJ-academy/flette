package com.example.flette.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	Optional<Answer> findByQuestionId(Integer questionId);
}
