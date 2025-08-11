package com.example.flette.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.dto.MemberDTO;
import com.example.flette.dto.QnaItemDTO;
import com.example.flette.entity.Answer;
import com.example.flette.entity.Member;
import com.example.flette.entity.Question;
import com.example.flette.repository.AnswerRepository;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.QuestionRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminApi {

	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	 QuestionRepository questionRepository;
	
	@Autowired
	AnswerRepository answerRepository;

	/**
	 * 회원 목록을 페이지별로 조회합니다. GET /api/admin/members?page=0&size=10
	 *
	 * @param page 조회할 페이지 번호 (0부터 시작)
	 * @param size 한 페이지당 보여줄 회원 수
	 * @return 페이지네이션된 회원 목록
	 */
	@GetMapping("/members")
	public ResponseEntity<Page<MemberDTO>> getMembers(
	        @PageableDefault(size = 10) Pageable pageable) {

	    Page<Member> page = memberRepository.findAll(pageable); // ✅ pageable 그대로 사용
	    Page<MemberDTO> body = page.map(MemberDTO::from);       // DTO 변환
	    return ResponseEntity.ok(body);
	}

	/**
	 * 특정 회원을 삭제합니다. DELETE /api/admin/members/{userid}
	 *
	 * @param userid 삭제할 회원의 아이디
	 * @return 삭제 성공 메시지
	 */
	@DeleteMapping("/members/{userid}")
	public ResponseEntity<String> deleteMember(@PathVariable String userid) {
		if (memberRepository.existsById(userid)) {
			memberRepository.deleteById(userid);
			return ResponseEntity.ok("회원 삭제 성공");
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	/** Q&A 목록 (페이징) */
    @GetMapping("/qna")
    public ResponseEntity<Page<QnaItemDTO>> getQnaList(
            @PageableDefault(size = 10, sort = "questionDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "false") boolean unanswered) {
        
        Page<Question> page;
        if (unanswered) {
            // 미답변만 조회
            page = questionRepository.findByStatusOrderByQuestionDateDesc(false, pageable);
        } else {
            // 전체 조회 (답변 완료 / 미답변 모두)
            page = questionRepository.findAll(pageable);
        }

        Page<QnaItemDTO> body = page.map(q -> {
            Optional<Answer> ans = answerRepository.findByQuestionId(q.getQuestionId());
            return QnaItemDTO.builder()
                    .questionId(q.getQuestionId())
                    .title(q.getTitle())
                    .writerMasked(mask(q.getUserid()))
                    .answered(ans.isPresent() || q.isStatus())
                    .questionDate(q.getQuestionDate())
                    .questionContent(q.getContent())
                    .answerContent(ans.map(Answer::getAnswerContent).orElse(null))
                    .answerDate(ans.map(Answer::getAnswerDate).orElse(null))
                    .build();
        });

        return ResponseEntity.ok(body);
    }

    /** Q&A 상세 */
    @GetMapping("/qna/{questionId}")
    public ResponseEntity<QnaItemDTO> getQna(@PathVariable Integer questionId) {
        return questionRepository.findById(questionId)
                .map(q -> {
                    Optional<Answer> ans = answerRepository.findByQuestionId(q.getQuestionId());
                    return ResponseEntity.ok(
                            QnaItemDTO.builder()
                                    .questionId(q.getQuestionId())
                                    .title(q.getTitle())
                                    .writerMasked(mask(q.getUserid()))
                                    .answered(ans.isPresent() || q.isStatus())
                                    .questionDate(q.getQuestionDate())
                                    .questionContent(q.getContent())
                                    .answerContent(ans.map(Answer::getAnswerContent).orElse(null))
                                    .answerDate(ans.map(Answer::getAnswerDate).orElse(null))
                                    .build()
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** 답변 등록 */
    @PostMapping("/qna/{questionId}/answer")
    @Transactional
    public ResponseEntity<QnaItemDTO> createAnswer(@PathVariable Integer questionId, @RequestBody Answer req) {
        Question q = questionRepository.findById(questionId).orElse(null);
        if (q == null) return ResponseEntity.notFound().build();

        Answer a = new Answer();
        a.setQuestionId(questionId); // ⚠ qeustionId 오타 매핑 주의
        a.setAnswerContent(req.getAnswerContent());
        a.setAnswerDate(new java.util.Date());
        answerRepository.save(a);

        q.setStatus(true);
        questionRepository.save(q);

        return getQna(questionId);
    }

    /** 답변 수정 */
    @PutMapping("/qna/{questionId}/answer")
    @Transactional
    public ResponseEntity<QnaItemDTO> updateAnswer(@PathVariable Integer questionId, @RequestBody Answer req) {
        Optional<Answer> ansOpt = answerRepository.findByQuestionId(questionId);
        if (ansOpt.isEmpty()) return ResponseEntity.notFound().build();

        Answer a = ansOpt.get();
        a.setAnswerContent(req.getAnswerContent());
        a.setAnswerDate(new java.util.Date());
        answerRepository.save(a);

        return getQna(questionId);
    }

    /** 답변 삭제 */
    @DeleteMapping("/qna/{questionId}/answer")
    @Transactional
    public ResponseEntity<Void> deleteAnswer(@PathVariable Integer questionId) {
        Optional<Answer> ansOpt = answerRepository.findByQuestionId(questionId);
        if (ansOpt.isPresent()) {
            answerRepository.delete(ansOpt.get());
            questionRepository.findById(questionId).ifPresent(q -> {
                q.setStatus(false);
                questionRepository.save(q);
            });
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /** 작성자 마스킹 */
    private String mask(String uid) {
        if (uid == null || uid.length() < 3) return "****";
        return uid.substring(0, Math.min(3, uid.length())) + "****";
    }
}