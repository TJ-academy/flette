package com.example.flette.api;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.flette.dto.MyPageStatsDTO;
import com.example.flette.entity.Member;
import com.example.flette.entity.Orders;
import com.example.flette.entity.Review;
import com.example.flette.entity.Question;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.OrdersRepository;
import com.example.flette.repository.ReviewRepository;
import com.example.flette.repository.QuestionRepository;

@RestController
@RequestMapping("/api/mypage")
public class MyPageApi {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrdersRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // 📌 마이페이지 통계
    @GetMapping("/stats")
    public ResponseEntity<MyPageStatsDTO> getStats(@RequestParam("userid") String userid) {
        int orderCount = orderRepository.countByUserid(userid);
        int reviewCount = reviewRepository.countByWriter(userid); // Review 엔티티에 writer 필드 있음
        int qnaCount = questionRepository.countByUserid(userid);

        MyPageStatsDTO stats = new MyPageStatsDTO(orderCount, reviewCount, qnaCount);
        return ResponseEntity.ok(stats);
    }

    // 📌 회원 정보 조회
    @GetMapping("/member/{userid}")
    public ResponseEntity<Member> getMemberInfo(@PathVariable("userid") String userid) {
        return memberRepository.findById(userid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 📌 회원 정보 수정
    @PostMapping("/member/update/{userid}")
    public ResponseEntity<String> updateMemberInfo(@PathVariable("userid") String userid,
                                                   @RequestBody Map<String, String> updates) {
        try {
            Optional<Member> memberOptional = memberRepository.findById(userid);
            if (memberOptional.isPresent()) {
                Member member = memberOptional.get();

                if (updates.containsKey("username")) member.setUsername(updates.get("username"));
                if (updates.containsKey("address1")) member.setAddress1(updates.get("address1"));
                if (updates.containsKey("address2")) member.setAddress2(updates.get("address2"));
                if (updates.containsKey("tel")) member.setTel(updates.get("tel"));
                if (updates.containsKey("zipcode")) member.setZipcode(updates.get("zipcode"));

                memberRepository.save(member);
                return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 정보 수정 실패: " + e.getMessage());
        }
    }

    // 📌 비밀번호 수정
    @PostMapping("/password/update/{userid}")
    public ResponseEntity<String> updatePassword(@PathVariable("userid") String userid,
                                                 @RequestBody Map<String, String> passwords) {
        try {
            Optional<Member> memberOptional = memberRepository.findById(userid);
            if (memberOptional.isPresent()) {
                String currentPwd = passwords.get("currentPwd");
                String newPwd = passwords.get("newPwd");

                if (memberRepository.checkPassword(userid, currentPwd) == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("기존 비밀번호가 올바르지 않습니다.");
                }
                memberRepository.updatePassword(userid, newPwd);
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("비밀번호 변경 실패: " + e.getMessage());
        }
    }

    // 📌 주문내역 조회
    @GetMapping("/orders/{userid}")
    public ResponseEntity<List<Orders>> getUserOrders(@PathVariable("userid") String userid) {
        List<Orders> orders = orderRepository.findByUserid(userid);
        return ResponseEntity.ok(orders);
    }

    // 📌 리뷰 조회
    @GetMapping("/reviews/{userid}")
    public ResponseEntity<Map<String, Object>> getUserReviews(@PathVariable("userid") String userid) {
        List<Review> rlist = reviewRepository.findByWriter(userid);
        Map<String, Object> result = new HashMap<>();
        result.put("rlist", rlist);
        return ResponseEntity.ok(result);
    }

    // 📌 문의(QnA) 조회
    @GetMapping("/qna/{userid}")
    public ResponseEntity<List<Question>> getUserQna(@PathVariable("userid") String userid) {
        List<Question> qlist = questionRepository.findByUseridOrderByQuestionDateDesc(userid);
        return ResponseEntity.ok(qlist);
    }
}
