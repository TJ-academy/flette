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
public class Flower {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "flower_id")
	private int flowerId;
	
	@Column(name = "add_price")
	private Integer addPrice;
	
	private String category;
	private String description;
	
	@Column(name = "flower_name")
	private String flowerName;
	
	@Column(name = "image_name")
	private String imageName;
	
	private String story;
	
	@Column(name ="`show`")
	private boolean show;
}