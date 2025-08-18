package com.example.flette.repository;

import com.example.flette.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByUseridOrderByQuestionDateDesc(String userid);

    // 답변이 완료되지 않은 질문만 페이징 조회
    Page<Question> findByStatusOrderByQuestionDateDesc(boolean status, Pageable pageable);

    Page<Question> findByProductId(Integer productId, Pageable pageable);

    // 기존 메서드 수정: Pageable을 추가하여 페이징을 처리
    Page<Question> findByUseridOrderByQuestionDateDesc(String userid, Pageable pageable);
    List<Question> findByProductId(Integer productId);

    // 암호화 질문 게시
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO question "
            + "(product_id, userid, title, content, status, passwd, question_date) VALUES "
            + "(:productId, :userid, :title, :content, false, SHA2(:passwd, 256), NOW())", 
            nativeQuery = true)
    void addQues(@Param("productId") Integer productId,
                 @Param("userid") String userid,
                 @Param("title") String title,
                 @Param("content") String content,
                 @Param("passwd") String passwd);

    // 비밀번호 확인용
    @Query(value = "SELECT question_id FROM question WHERE question_id = :questionId AND passwd = SHA2(:passwd, 256)", nativeQuery = true)
    Optional<Integer> checkPassword(@Param("questionId") Integer questionId, @Param("passwd") String passwd);

    // 해당 userid로 작성된 문의 개수를 조회
    long countByUserid(String userid);
}
