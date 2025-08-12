package com.example.flette.entity;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @JsonIgnore // 순환 참조 방지
    @OneToMany(mappedBy = "member")
    private List<Cart> cartList;

    @PrePersist
    public void prePersist() {
        level = level == 0 ? 1 : level;
    }
}
