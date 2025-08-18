package com.example.flette.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.flette.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
	List<Question> findByUseridOrderByQuestionDateDesc(String userid);
    int countByUserid(String userid);
    
	// 답변이 완료되지 않은 질문만 페이징 조회
	Page<Question> findByStatusOrderByQuestionDateDesc(boolean status, Pageable pageable);
	
	List<Question> findByProductId(Integer productId);
	
	// 암호화 질문 게시
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO Question "
	        + "(productId, userid, title, content, passwd, questionDate) VALUES "
	        + "(:productId, :userid, :title, :content, SHA2(:passwd, 256), NOW())", 
	        nativeQuery = true)
	void addQues(@Param("productId") Integer productId,
	             @Param("userid") String userid,
	             @Param("title") String title,
	             @Param("content") String content,
	             @Param("passwd") String passwd);
	//비밀번호 확인용
    @Query(value = "SELECT questionId FROM Question WHERE questionId = :questionId AND passwd = SHA2(:passwd, 256)", nativeQuery = true)
    String checkPassword(@Param("questionId") Integer questionId, @Param("passwd") String passwd);
}
