package com.example.flette.repository;

import java.util.List;
import java.util.Optional;

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
	
	// 답변이 완료되지 않은 질문만 페이징 조회
	Page<Question> findByStatusOrderByQuestionDateDesc(boolean status, Pageable pageable);
	
	Page<Question> findByProductId(Integer productId, Pageable pageable);
	
	// 암호화 질문 게시
    @Modifying
    @Transactional
    @Query(value = "insert into question "
            + "(product_id, userid, title, content, status, passwd, question_date) values "
            + "(:productId, :userid, :title, :content, false, SHA2(:passwd, 256), now())", nativeQuery = true)
    void addQues(@Param("productId") Integer productId, @Param("userid") String userid, 
    		@Param("title") String title, @Param("content") String content, 
    		@Param("passwd") String passwd);
	
	//비밀번호 확인용
    @Query(value = "SELECT question_id FROM Question WHERE question_id = :questionId AND passwd = SHA2(:passwd, 256)", nativeQuery = true)
    Optional<Integer> checkPassword(@Param("questionId") Integer questionId, @Param("passwd") String passwd);
}
