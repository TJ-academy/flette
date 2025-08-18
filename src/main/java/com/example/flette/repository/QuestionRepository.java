package com.example.flette.repository;

import com.example.flette.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByUseridOrderByQuestionDateDesc(String userid);

    // 답변이 완료되지 않은 질문만 페이징 조회
    Page<Question> findByStatusOrderByQuestionDateDesc(boolean status, Pageable pageable);

    // 상품별 문의 페이징
    Page<Question> findByProductId(Integer productId, Pageable pageable);

    // 상품별 문의 (페이징 없이)
    List<Question> findByProductId(Integer productId);

    // 사용자별 문의 (페이징)
    Page<Question> findByUseridOrderByQuestionDateDesc(String userid, Pageable pageable);

    // 🔑 비밀번호 확인 (SHA256 비교)
    @Query(value = "SELECT question_id FROM question WHERE question_id = :questionId AND passwd = SHA2(:passwd, 256)", nativeQuery = true)
    Optional<Integer> checkPassword(@Param("questionId") Integer questionId, @Param("passwd") String passwd);

    // 해당 userid로 작성된 문의 개수
    long countByUserid(String userid);
    
    
}

