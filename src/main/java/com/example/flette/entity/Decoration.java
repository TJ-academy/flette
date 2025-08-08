package com.example.flette.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Decoration {
	@Id
	private int decorationId;
	
	private String decorationName;
	private Integer utilPrice; //단가
	private String description;
}
