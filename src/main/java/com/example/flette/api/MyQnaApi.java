// com.example.flette.api.MyQnaApi
package com.example.flette.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<QnaItemDTO>> myQnaList(@RequestParam String userid) {
        List<Question> list = questionRepository.findByUseridOrderByQuestionDateDesc(userid);

        List<QnaItemDTO> result = list.stream().map(q -> {
            Optional<Answer> ansOpt = answerRepository.findByQuestionId(q.getQuestionId());
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
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 상세
    @GetMapping("/{questionId}")
    public ResponseEntity<QnaItemDTO> qnaDetail(@PathVariable Integer questionId) {
        return questionRepository.findById(questionId)
                .map(q -> {
                    Optional<Answer> ansOpt = answerRepository.findByQuestionId(q.getQuestionId());
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
}
