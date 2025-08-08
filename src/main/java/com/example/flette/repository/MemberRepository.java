package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.flette.entity.Member;

public interface MemberRepository extends JpaRepository<Member, String> {
	
	//암호화 로그인
	@Query(value = "select * from member where userid = :userid and passwd = standard_hash(:passwd, 'SHA256')", nativeQuery = true)
	Member findByLogin(@Param("id") String userid, @Param("passwd") String passwd);
	
	//암호화 회원가입
	@Modifying
	@Transactional
	@Query(value = "insert into member "
			+ "(userid, passwd, username, zipcode, address1, address2, tel, joined_at) values "
			+ "(:id, starndard_hash(:passwd, 'SHA256'), :username, :zipcode, :address1, :address2, :tel, sysdate)", nativeQuery = true)
	void register(@Param("userid") String userid, @Param("passwd") String passwd, @Param("username") String username, @Param("zipcode") String zipcode,
			@Param("address1") String address1, @Param("address2") String address2, @Param("tel") String tel);
}
