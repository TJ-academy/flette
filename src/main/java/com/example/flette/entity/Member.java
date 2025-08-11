package com.example.flette.entity;

import java.util.Date;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
	@Id
	private String userid;

	private String passwd;
	private String username;

	@ColumnDefault("1")
	private Integer level;

	private String zipcode;
	private String address1;
	private String address2;
	private String tel;
	
	@Column(name = "joined_at")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date joinedAt;

	/*
	@OneToMany(mappedBy = "member")
	List<Cart> cartList = new ArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "member")
	List<Review> reviewList = newArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "member")
	List<Question> questionList = newArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "member")
	List<Answer> answerList = newArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "member")
	List<OrderItem> orderItemList = newArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "user")
	List<Summary> summaryList = newArrayList<>();

	*/
    public Member(String userid) {
        this.userid = userid;
    }

    @PrePersist
    public void prePersist() {
        level = level == 0 ? 1 : level;
    }
}
