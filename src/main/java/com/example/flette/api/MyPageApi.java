package com.example.flette.api;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.entity.Member;
import com.example.flette.repository.MemberRepository;
import com.example.flette.repository.OrdersRepository;
import com.example.flette.repository.QuestionRepository;
import com.example.flette.repository.ReviewRepository;

@RestController
@RequestMapping("/api/mypage")
public class MyPageApi {

	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private QuestionRepository questionRepository;

	// 사용자별 주문내역, 리뷰, 문의 개수를 반환하는 API
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(@RequestParam(name = "userid") String userid) {
    	String userId = userid;
        // 각 테이블에서 해당 userid 기준으로 개수 조회
        long ordersCount = ordersRepository.countByUserId(userId);
        long reviewsCount = reviewRepository.countByWriter(userid);
        long questionsCount = questionRepository.countByUserid(userid);

        // 결과를 Map 형태로 반환
        return ResponseEntity.ok(Map.of(
            "ordersCount", ordersCount,
            "reviewsCount", reviewsCount,
            "questionsCount", questionsCount
        ));
    }
	
	// 회원 정보 조회
	@GetMapping("/member/{userid}")
	public ResponseEntity<Member> getMemberInfo(@PathVariable("userid") String userid) {
		Optional<Member> member = memberRepository.findById(userid);
		if (member.isPresent()) {
			return ResponseEntity.ok(member.get());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	// 회원 정보 수정 (이름, 주소, 전화번호)
	@PostMapping("/member/update/{userid}")
	public ResponseEntity<String> updateMemberInfo(@PathVariable("userid") String userid, @RequestBody Map<String, String> updates) {
		try {
			Optional<Member> memberOptional = memberRepository.findById(userid);
			if (memberOptional.isPresent()) {
				Member member = memberOptional.get();

				// 업데이트할 정보만 받아서 수정
				if (updates.containsKey("username")) {
					member.setUsername(updates.get("username"));
				}
				if (updates.containsKey("address1")) {
					member.setAddress1(updates.get("address1"));
				}
				if (updates.containsKey("address2")) {
					member.setAddress2(updates.get("address2"));
				}
				if (updates.containsKey("tel")) {
					member.setTel(updates.get("tel"));
				}
                if (updates.containsKey("zipcode")) {
					member.setZipcode(updates.get("zipcode"));
				}


				memberRepository.save(member); // JpaRepository의 save 메서드로 업데이트
				return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 정보 수정 실패: " + e.getMessage());
		}
	}

    // 비밀번호 수정
    @PostMapping("/password/update/{userid}")
    public ResponseEntity<String> updatePassword(@PathVariable("userid") String userid, @RequestBody Map<String, String> passwords) {
        try {
            Optional<Member> memberOptional = memberRepository.findById(userid);
            if (memberOptional.isPresent()) {
                Member member = memberOptional.get();
                String currentPwd = passwords.get("currentPwd");
                String newPwd = passwords.get("newPwd");

                // 기존 비밀번호가 맞는지 확인 (암호화된 비밀번호와 비교)
                if (memberRepository.checkPassword(userid, currentPwd) == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("기존 비밀번호가 올바르지 않습니다.");
                }

                // 새로운 비밀번호로 업데이트 (암호화하여 저장)
                memberRepository.updatePassword(userid, newPwd);

                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 실패: " + e.getMessage());
        }
    }
    
    //회원탈퇴
    @DeleteMapping("/member/delete/{userid}")
    public ResponseEntity<String> deleteMember(@PathVariable("userid") String userid) {
    	try {
    		Optional<Member> memberOptional = memberRepository.findById(userid);
    		if(memberOptional.isPresent()) {
    			memberRepository.deleteById(userid);
    			return ResponseEntity.ok("이용해주셔서 감사합니다.");
    		} else {
    			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 회원 정보를 찾을 수 없습니다.");    			
    		}
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원탈퇴 처리 중 오류가 발생했습니다.");
		}
    }
    
}