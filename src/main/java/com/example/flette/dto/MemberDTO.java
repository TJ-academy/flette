package com.example.flette.dto;

import java.util.Date;

import com.example.flette.entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {
	private String userid;
	private String username;
	private Integer level;
	private String zipcode;
	private String address1;
	private String address2;
	private String tel;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date joinedAt;

	public static MemberDTO from(Member m) {
		MemberDTO d = new MemberDTO();
		d.setUserid(m.getUserid());
		d.setUsername(m.getUsername());
		d.setLevel(m.getLevel());
		d.setZipcode(m.getZipcode());
		d.setAddress1(m.getAddress1());
		d.setAddress2(m.getAddress2());
		d.setTel(m.getTel());
		d.setJoinedAt(m.getJoinedAt());
		return d;
	}
}