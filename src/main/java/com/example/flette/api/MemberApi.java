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
	        memberRepository.register(
	            member.getUserid(), 
	            member.getPasswd(), 
	            member.getUsername(), 
	            member.getZipcode(), 
	            member.getAddress1(), 
	            member.getAddress2(), 
	            member.getTel()
	        );
	        return ResponseEntity.ok("회원가입 성공");
	    } catch (Exception e) {
	        // 에러 처리
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패");
	    }
	}
	
	@GetMapping("/exist")
	public String exist(@RequestParam("userid") String userid) {
		boolean exists = memberRepository.existsById(userid);
		if (exists) {
			return "이미 존재";
		} else {
			return "사용 가능";
		}
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
	public void logout(HttpSession session) {
		session.invalidate();
	}
}
