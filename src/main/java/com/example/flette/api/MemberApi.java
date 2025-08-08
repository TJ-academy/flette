package com.example.flette.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flette.entity.Member;
import com.example.flette.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/member")
public class MemberApi {

	@Autowired
	MemberRepository memberRepository;
	
	// MemberApi.java
	@PostMapping("/insert")
	public ResponseEntity<String> insert(@RequestBody Member member) {
	    try {
	        // 프론트엔드에서 보낸 'name' 필드를 'username'으로 매핑
	        // @RequestBody가 자동으로 처리하지만, 명시적으로 확인 가능
	        String username = member.getUsername();
	        if (username == null || username.isEmpty()) {
	            // 이름이 없는 경우 에러 처리
	            return ResponseEntity.badRequest().body("이름을 입력해주세요.");
	        }

	        // 프론트엔드에서 zipcode가 비어있는 경우를 대비한 처리 (null 허용)
	        String zipcode = member.getZipcode();
	        if (zipcode == null) {
	            // zipcode가 필수 필드가 아니라면 null을 허용하거나, 기본값 설정
	            // member.setZipcode("");
	        }

	        // 프론트엔드에서 tel이 비어있는 경우를 대비한 처리 (null 허용)
	        String tel = member.getTel();
	        if (tel == null) {
	            // tel이 필수 필드가 아니라면 null을 허용하거나, 기본값 설정
	            // member.setTel(""); 
	        }

	        memberRepository.register(
	            member.getUserid(), 
	            member.getPasswd(), 
	            member.getUsername(), // 'name' 대신 'username' 필드 사용
	            member.getZipcode(), 
	            member.getAddress1(), 
	            member.getAddress2(), 
	            member.getTel()
	        );
	        return ResponseEntity.ok("회원가입 성공");
	    } catch (Exception e) {
	        // 에러 로그 출력
	        e.printStackTrace(); 
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패: " + e.getMessage());
	    }
	}
	
	@GetMapping("/exist")
	public boolean exist(@RequestParam("userid") String userid) {
		boolean exists = memberRepository.existsById(userid);
		return exists;
	}
	
	@PostMapping("/login")
	public ResponseEntity<Member> login(@RequestBody Member m, HttpSession session) {
		Member member = memberRepository.findByLogin(m.getUserid(), m.getPasswd());
		if (member != null) {
			session.setAttribute("loginId", member.getUserid());
			session.setAttribute("loginName", member.getUsername());
			session.setAttribute("role", member.getLevel());
			return ResponseEntity.ok(member);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpSession session) {
	    session.invalidate();  // 세션 무효화
	    return ResponseEntity.ok("로그아웃 성공");
	}
}
