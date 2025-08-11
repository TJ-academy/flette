package com.example.flette.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
	List<Question> findByUseridOrderByQuestionDateDesc(String userid);
}
