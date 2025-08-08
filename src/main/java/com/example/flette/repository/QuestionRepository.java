package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flette.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

}
