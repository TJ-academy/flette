package com.example.flette.api;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.dto.QnaItemDTO;
import com.example.flette.entity.Answer;
import com.example.flette.entity.Question;
import com.example.flette.repository.AnswerRepository;
import com.example.flette.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mypage/qna")
@RequiredArgsConstructor
public class MyQnaApi {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    // 목록: 내가 쓴 문의
    @GetMapping
    public ResponseEntity<Page<QnaItemDTO>> myQnaList(
        @RequestParam(name = "userid") String userid,
        @RequestParam(name = "page") int page,  // 페이지 번호
        @RequestParam(name = "size") int size)  // 페이지 크기
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("questionDate")));
        Page<Question> questionsPage = questionRepository.findByUseridOrderByQuestionDateDesc(userid, pageable);

        Page<QnaItemDTO> result = questionsPage.map(q -> {
            Optional<Answer> ansOpt = answerRepository.findByQuestion_QuestionId(q.getQuestionId());
            return QnaItemDTO.builder()
                    .questionId(q.getQuestionId())
                    .title(q.getTitle())
                    .writerMasked(maskUser(q.getUserid()))
                    .answered(ansOpt.isPresent() || q.isStatus())
                    .questionDate(q.getQuestionDate())
                    .questionContent(q.getContent())
                    .answerContent(ansOpt.map(Answer::getAnswerContent).orElse(null))
                    .answerDate(ansOpt.map(Answer::getAnswerDate).orElse(null))
                    .build();
        });

        return ResponseEntity.ok(result);
    }

    // 상세
    @GetMapping("/{questionId}")
    public ResponseEntity<QnaItemDTO> qnaDetail(@PathVariable("questionId") Integer questionId) {
        return questionRepository.findById(questionId)
                .map(q -> {
                    Optional<Answer> ansOpt = answerRepository.findByQuestion_QuestionId(q.getQuestionId());
                    QnaItemDTO dto = QnaItemDTO.builder()
                            .questionId(q.getQuestionId())
                            .title(q.getTitle())
                            .writerMasked(maskUser(q.getUserid()))
                            .answered(ansOpt.isPresent() || q.isStatus())
                            .questionDate(q.getQuestionDate())
                            .questionContent(q.getContent())
                            .answerContent(ansOpt.map(Answer::getAnswerContent).orElse(null))
                            .answerDate(ansOpt.map(Answer::getAnswerDate).orElse(null))
                            .build();
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private String maskUser(String uid) {
        if (uid == null || uid.length() < 3) return "****";
        return uid.substring(0, Math.min(3, uid.length())) + "****";
    }
    
    // 문의 삭제
    @Transactional
    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("questionId") Integer questionId) {
        // 먼저 Answer 테이블에서 해당 question_id와 연관된 답변을 삭제
        answerRepository.deleteByQuestion_QuestionId(questionId);  // 이 메서드는 AnswerRepository에서 정의되어야 합니다.

        // 그런 다음 Question 테이블에서 질문을 삭제
        Optional<Question> questionOptional = questionRepository.findById(questionId);
        if (questionOptional.isPresent()) {
            questionRepository.deleteById(questionId);  // 해당 질문 삭제
            return ResponseEntity.ok().build(); // 삭제 성공
        } else {
            return ResponseEntity.notFound().build(); // 질문이 존재하지 않으면 404 반환
        }
    }


}