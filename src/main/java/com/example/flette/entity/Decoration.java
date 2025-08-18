package com.example.flette.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int decorationId;
	
	private String decorationName;
	private String category;
	private Integer utilPrice;
	private String description;
	
	@Column(name = "image_name")
	private String imageName;
	
	@Column(name ="`show`")
	private boolean show;
}
