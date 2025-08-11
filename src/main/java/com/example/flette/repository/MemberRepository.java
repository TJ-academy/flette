package com.example.flette.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.flette.entity.Member;

public interface MemberRepository extends JpaRepository<Member, String> {

    // 암호화 로그인
    @Query(value = "select * from member where userid = :userid and passwd = SHA2(:passwd, 256)", nativeQuery = true)
    Member findByLogin(@Param("userid") String userid, @Param("passwd") String passwd);

    // 암호화 회원가입
    @Modifying
    @Transactional
    @Query(value = "insert into member "
            + "(userid, passwd, username, zipcode, address1, address2, tel, joined_at) values "
            + "(:userid, SHA2(:passwd, 256), :username, :zipcode, :address1, :address2, :tel, now())", nativeQuery = true)
    void register(@Param("userid") String userid, @Param("passwd") String passwd, @Param("username") String username, @Param("zipcode") String zipcode,
                  @Param("address1") String address1, @Param("address2") String address2, @Param("tel") String tel);
    
    // 회원 정보 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.username = :username, m.address1 = :address1, m.address2 = :address2, m.tel = :tel WHERE m.userid = :userid")
    void updateMemberInfo(@Param("userid") String userid, @Param("username") String username, @Param("address1") String address1, @Param("address2") String address2, @Param("tel") String tel);
    
    // 회원 ID로 회원 정보 조회
    Member findByUserid(String userid);

    // 비밀번호 변경
    @Modifying
    @Transactional
    @Query(value = "UPDATE Member SET passwd = SHA2(:newPwd, 256) WHERE userid = :userid", nativeQuery = true)
    void updatePassword(@Param("userid") String userid, @Param("newPwd") String newPwd);
    
    // 비밀번호 확인용 쿼리
    @Query(value = "SELECT userid FROM Member WHERE userid = :userid AND passwd = SHA2(:currentPwd, 256)", nativeQuery = true)
    String checkPassword(@Param("userid") String userid, @Param("currentPwd") String currentPwd);
}